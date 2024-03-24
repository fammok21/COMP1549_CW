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

public class ChatServer {

    private static List<String> names = new ArrayList<>();
    private static List<PrintWriter> writers = new ArrayList<>();
    private static Coordinator coordinator = null;

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);

        // Start a thread to periodically check and display active members
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20000); // 20 seconds
                    synchronized (names) {
                        System.out.println("Active Group Members: " + String.join(", ", names));
                        // Optional: Here, you could also invoke coordinator.checkClients(names) if it fits your design
                    }
                } catch (InterruptedException e) {
                    System.out.println("Coordinator's periodic check interrupted.");
                }
            }
        }).start();

        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
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
                            if (coordinator == null) {
                                coordinator = new Coordinator(new User(socket.getInetAddress().getHostAddress(), socket.getPort(), name));
                                out.println("MESSAGE You are now the coordinator.");
                            } else {
                                out.println("MESSAGE Current coordinator is: " + coordinator.user.getUsername());
                            }
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                broadcastMessage("MESSAGE " + name + " has joined");
                out.println("MESSAGE Current coordinator is: " + (coordinator != null ? coordinator.user.getUsername() : "not set"));

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    if (input.startsWith("@")) {
                        handlePrivateMessage(input);
                    } else {
                        broadcastMessage("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                cleanUp();
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter writer : writers) {
                writer.println(message);
            }
        }

        private void handlePrivateMessage(String input) {
            int firstSpace = input.indexOf(" ");
            if (firstSpace != -1) {
                String targetName = input.substring(1, firstSpace);
                String message = input.substring(firstSpace + 1);
                sendMessageToUser(targetName, name, message);
            }
        }

        private void sendMessageToUser(String targetName, String senderName, String message) {
            synchronized (this) {
                int index = names.indexOf(targetName);
                if (index != -1) {
                    PrintWriter writer = writers.get(index);
                    if (writer != null) {
                        writer.println("MESSAGE (private) from " + senderName + ": " + message);
                        // Echo the private message back to the sender
                        out.println("MESSAGE (private) to " + targetName + ": " + message);
                    }
                } else {
                    out.println("MESSAGE User " + targetName + " not found.");
                }
            }
        }

        private void cleanUp() {
            if (name != null && !name.isEmpty()) {
                synchronized (this) {
                    names.remove(name);
                    writers.remove(out);
                }
                broadcastMessage("MESSAGE " + name + " has left");
                if (coordinator != null && name.equals(coordinator.user.getUsername())) {
                    selectNewCoordinator();
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not close a socket: " + e.getMessage());
            }
        }

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
