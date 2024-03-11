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

    // Lists to store client names and corresponding output streams (for message sending)
    private static List<String> names = new ArrayList<>();
    private static List<PrintWriter> writers = new ArrayList<>();

    // Coordinator instance to manage coordination tasks
    private static Coordinator coordinator = null;

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");

        // Creating a thread pool for handling multiple client connections
        ExecutorService pool = Executors.newFixedThreadPool(500);

        try (ServerSocket listener = new ServerSocket(59001)) {
            // Continuously accept incoming client connections
            while (true) {
                // Assign a thread from the pool to handle each client connection
                pool.execute(new Handler(listener.accept()));

                // Create a new thread to run the checkClients method
                Thread coordinatorThread = new Thread(() -> coordinator.checkClients(names));
                coordinatorThread.start(); // Start the thread
            }
        }
    }

    // Inner class to handle each client connection
    private static class Handler implements Runnable {
        private String name;         // Client's name
        private Socket socket;       // Client's socket
        private Scanner in;          // Input stream from client
        private PrintWriter out;     // Output stream to client

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Setting up input and output streams for communication with the client
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Client registration loop
                while (true) {
                    out.println("SUBMITNAME"); // Request client to submit a name
                    name = in.nextLine();      // Receive name from client
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        // Check if the name is not empty and not already taken
                        if (!name.isEmpty() && !names.contains(name)) {
                            names.add(name); // Add the name to the list of names
                            break;           // Break out of the loop
                        }
                    }
                }

                // If no coordinator has been set yet, set it to the current client
                if (coordinator == null) {
                    User coordinatorUser = new User("localhost", 59001, name); // Assuming localhost and port 59001
                    coordinator = new Coordinator(coordinatorUser);
                    System.out.println("Coordinator is " + coordinator.user.getUsername());
                    out.println("COORDINATOR " + name); // Inform client they are the coordinator
                    // Inform other clients about the new coordinator
                    for (PrintWriter writer : writers) {
                        writer.println("COORDINATOR " + coordinator.user.getUsername() + " has joined");
                    }
                }

                out.println("NAMEACCEPTED " + name); // Inform the client that the name was accepted

                // Inform all clients about the new client joining
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }

                writers.add(out); // Add the client's output stream to the list of writers
                out.println("WELCOME " + coordinator.user.getUsername());
                // Continuously receive and broadcast messages from the client
                while (true) {
                    String input = in.nextLine(); // Receive message from client
                    if (input.toLowerCase().startsWith("query")) {
                        // Send the list of names back to the client
                        out.println("NAMES " + String.join(", ", names));
                    }
                    if (input.toLowerCase().startsWith("/quit")) {
                        return; // Exit loop if client wants to quit
                    }
                    // Broadcast the message to all clients
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                // Cleanup resources when the client disconnects
                if (out != null) {
                    writers.remove(out); // Remove the client's output stream
                }
                if (name != null) {
                    System.out.println(name + " is leaving");
                    names.remove(name); // Remove the client's name
                    // Inform all clients about the leaving client
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }
                try {
                    socket.close(); // Close the client's socket
                } catch (IOException e) {
                    // Handle socket closure exception
                }
                // Handle coordinator selection if leaving client was the coordinator
                if (coordinator != null && name.equals(coordinator.user.getUsername())) {
                    // Select a new coordinator if other clients are present
                    if (!names.isEmpty()) {
                        User newCoordinatorUser = new User("localhost", 59001, names.get(0)); // Assuming localhost and port 59001
                        coordinator = new Coordinator(newCoordinatorUser); // Choose the first client as the new coordinator
                        System.out.println("Coordinator is now " + coordinator.user.getUsername());
                        // Inform all clients about the new coordinator
                        for (PrintWriter writer : writers) {
                            writer.println("NEW_COORDINATOR " + coordinator.user.getUsername());
                        }
                    } else {
                        coordinator = null; // Reset coordinator to null if no clients remain
                    }
                }
            }
        }
    }
}
