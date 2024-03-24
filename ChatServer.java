package week3;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// The ChatServer class manages client connections, messages, and coordinates chat functionalities.
public class ChatServer {

    private static List<String> names = new ArrayList<>(); // Stores unique names of connected clients
    private static List<PrintWriter> writers = new ArrayList<>(); // Stores PrintWriter objects for all clients to broadcast messages
    private static Coordinator coordinator = null; // Stores the coordinator's details

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500); // Thread pool for handling client connections

        // Start a thread to periodically check and display active members
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20000); // Sleep for 20 seconds
                    synchronized (names) {
                        // Display active group members every 20 seconds
                        System.out.println("Active Group Members: " + String.join(", ", names));
                    }
                } catch (InterruptedException e) {
                    System.out.println("Coordinator's periodic check interrupted.");
                }
            }
        }).start();

        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                // Accept new client connections and handle them in the thread pool
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    // Inner class to handle individual client connections
    private static class Handler implements Runnable {
        private String name; // Stores the client's name
        private Socket socket; // Socket to communicate with the client
        private Scanner in; // Scanner to read messages from the client
        private PrintWriter out; // PrintWriter to send messages to the client

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Client name registration loop
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null || name.isEmpty() || names.contains(name)) {
                        return; // Reject connection if name is invalid or already taken
                    }
                    synchronized (this) {
                        if (!names.contains(name)) {
                            names.add(name);
                            writers.add(out);
                            if (coordinator == null) {
                                // Assign the first connected user as the coordinator
                                coordinator = new Coordinator(new User(socket.getInetAddress().getHostAddress(), socket.getPort(), name));
                                out.println("MESSAGE You are now the coordinator.");
                            } else {
                                out.println("MESSAGE Current coordinator is: " + coordinator.user.getUsername());
                            }
                            break; // Exit the loop once a unique name is submitted
                        }
                    }
                }

                // Notify others that a new client has joined
                out.println("NAMEACCEPTED " + name);
                broadcastMessage("MESSAGE " + name + " has joined");
                out.println("MESSAGE Current coordinator is: " + (coordinator != null ? coordinator.user.getUsername() : "not set"));

                // Main loop to process incoming messages
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return; // Handle client disconnection
                    }
                    if (input.startsWith("@")) {
                        // Handle private messages
                        handlePrivateMessage(input);
                    } else {
                        // Broadcast public messages
                        broadcastMessage("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                // Cleanup resources on disconnection
                cleanUp();
            }
        }

        // Broadcasts a message to all clients
        private void broadcastMessage(String message) {
            for (PrintWriter writer : writers) {
                writer.println(message);
            }
        }

        // Handles sending a private message to a specific user
        private void handlePrivateMessage(String input) {
            int firstSpace = input.indexOf(" ");
            if (firstSpace != -1) {
                String targetName = input.substring(1, firstSpace);
                String message = input.substring(firstSpace + 1);
                sendMessageToUser(targetName, name, message);
            }
        }

        // Sends a private message to a user if they are connected
        private void sendMessageToUser(String targetName, String senderName, String message) {
            synchronized (this) {
                int index = names.indexOf(targetName);
                if (index != -1) {
                    PrintWriter writer = writers.get(index);
                    writer.println("MESSAGE (private) from " + senderName + ": " + message);
                    // Echo the private message back to the sender
                    out.println("MESSAGE (private) to " + targetName + ": " + message);
                } else {
                    out.println("MESSAGE User " + targetName + " not found.");
                }
            }
        }

        // Cleans up resources when a client disconnects and selects a new coordinator if necessary
        private void cleanUp() {
            if (name != null && !name.isEmpty()) {
                synchronized (this) {
                    names.remove(name);
                    writers.remove(out);
                    broadcastMessage("MESSAGE " + name + " has left");
                    if (coordinator != null && name.equals(coordinator.user.getUsername())) {
                        selectNewCoordinator();
                    }
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not close a socket: " + e.getMessage());
            }
        }

        // Selects a new coordinator among the remaining connected clients
        private void selectNewCoordinator() {
            if (!names.isEmpty()) {
                String newCoordinatorName = names.get(0);
                coordinator = new Coordinator(new User(socket.getInetAddress().getHostAddress(), socket.getPort(), newCoordinatorName));
                broadcastMessage("NEW_COORDINATOR " + newCoordinatorName);
            } else {
                coordinator = null; // Reset if no users are left
            }
        }
    }
}

