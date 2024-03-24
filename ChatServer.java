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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatServer {

    // List of all the client names and their print writers for message broadcasting.
    private static final List<String> names = new ArrayList<>();
    private static final List<PrintWriter> writers = new ArrayList<>();
    
    // Coordinator management using a Singleton Pattern.
    private static Coordinator coordinator = null;
    
    // Executor for scheduled tasks, like updating the active user list.
    private static final ScheduledExecutorService userUpdateScheduler = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);

        // Schedule a task to update the console with the active user list every 20 seconds.
        userUpdateScheduler.scheduleAtFixedRate(() -> System.out.println("Active users: " + String.join(", ", names)),
            0, 20, TimeUnit.SECONDS);

        // Shutdown hook to clean up resources upon server shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down chat server...");
            pool.shutdown();
            userUpdateScheduler.shutdownNow();
        }));

        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                try {
                    Socket socket = listener.accept();
                    pool.execute(new Handler(socket));
                } catch (IOException e) {
                    System.err.println("Could not accept client connection: " + e.getMessage());
                }
            }
        }
    }

    private static class Handler implements Runnable {
        private String name;
        private final Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Client name registration.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null || name.isEmpty() || names.contains(name)) {
                        return;
                    }
                    synchronized (this) {
                        if (!names.contains(name)) {
                            names.add(name);
                            writers.add(out);
                            // First user becomes the coordinator.
                            if (coordinator == null) {
                                coordinator = new Coordinator(new User(socket.getInetAddress().getHostAddress(), socket.getPort(), name));
                                broadcastMessage("MESSAGE " + name + " is now the coordinator.");
                            } else {
                                out.println("MESSAGE Current coordinator is: " + coordinator.user.getUsername());
                            }
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                broadcastMessage("MESSAGE " + name + " has joined");

                // Message processing.
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    if (input.startsWith("@")) {
                        handlePrivateMessage(input);
                        out.println("MESSAGE (private) to " + input.substring(1, input.indexOf(' ')) + ": " + input.substring(input.indexOf(' ') + 1));
                    } else {
                        broadcastMessage("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error handling client " + name + ": " + e.getMessage());
            } finally {
                cleanUp();
            }
        }

        // Broadcasts a message to all clients.
        private void broadcastMessage(String message) {
            writers.forEach(writer -> writer.println(message));
        }

        // Handles private messages between clients.
        private void handlePrivateMessage(String input) {
            int firstSpace = input.indexOf(' ');
            if (firstSpace != -1) {
                String targetName = input.substring(1, firstSpace);
                String message = input.substring(firstSpace + 1);
                sendMessageToUser(targetName, name, message);
            }
        }

        // Sends a private message to a specific user.
        private void sendMessageToUser(String targetName, String senderName, String message) {
            synchronized (this) {
                int index = names.indexOf(targetName);
                if (index != -1) {
                    PrintWriter writer = writers.get(index);
                    writer.println("MESSAGE (private) from " + senderName + ": " + message);
                } else {
                    out.println("MESSAGE User " + targetName + " not found.");
                }
            }
        }

        // Cleans up resources when a client disconnects and reassigns coordinator if necessary.
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
                System.err.println("Could not close the socket for " + name + ": " + e.getMessage());
            }
        }

        // Reassigns the coordinator role if the current coordinator leaves.
        private void selectNewCoordinator() {
            synchronized (this) {
                if (!names.isEmpty()) {
                    String newCoordinatorName = names.get(0);
                    coordinator = new Coordinator(new User(socket.getInetAddress().getHostAddress(), socket.getPort(), newCoordinatorName));
                    broadcastMessage("NEW_COORDINATOR " + newCoordinatorName);
                } else {
                    coordinator = null; // Reset if no users are left
                    // Notify all that there's currently no coordinator
                    broadcastMessage("MESSAGE No coordinator is currently assigned.");
                }
            }
        }
    }
}
