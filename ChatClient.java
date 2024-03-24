package week3;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

// The ChatClient class represents a client in the chat application.
public class ChatClient {
    User user; // User object to store user information
    Scanner in; // Scanner to read messages from the server
    PrintWriter out; // PrintWriter to send messages to the server
    JFrame frame = new JFrame("Chatter"); // JFrame for the chat client window
    JTextField textField = new JTextField(50); // TextField for entering messages
    JTextArea messageArea = new JTextArea(16, 50); // TextArea for displaying messages

    // Constructor to initialize the ChatClient with a user object
    public ChatClient(User user) {
        this.user = user;

        // GUI setup
        textField.setEditable(false); // Disable message input until connected
        messageArea.setEditable(false); // Disable message area editing
        frame.getContentPane().add(textField, BorderLayout.SOUTH); // Add textField to the bottom
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER); // Add messageArea in the center with scroll
        frame.pack(); // Pack the frame to fit its components

        // Action listener for the TextField to send messages when enter is pressed
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText()); // Send message to server
                textField.setText(""); // Clear TextField after sending
            }
        });
    }

    // Method to run the chat client
    private void run() throws IOException {
        try {
            Socket socket = new Socket(user.getServerAddress(), user.getPort()); // Connect to server
            in = new Scanner(socket.getInputStream()); // Create scanner to read from server
            out = new PrintWriter(socket.getOutputStream(), true); // Create PrintWriter to write to server, auto flush on

            out.println(user.getUsername()); // Send username to server

            // Main loop to receive messages from server
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("NAMEACCEPTED")) {
                    frame.setTitle("Chatter - " + user.getUsername()); // Update frame title with username
                    textField.setEditable(true); // Enable TextField for sending messages
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n"); // Append received message to messageArea
                } else if (line.startsWith("COORDINATOR")) {
                    // Handle coordinator message
                    String coordinatorName = line.substring(12); // Extract coordinator name
                    messageArea.append("The coordinator is now: " + coordinatorName + "\n"); // Display coordinator name
                } else if (line.startsWith("NEW_COORDINATOR")) {
                    // Handle new coordinator message
                    String newCoordinatorName = line.substring(16); // Extract new coordinator name
                    messageArea.append("A new coordinator has been appointed: " + newCoordinatorName + "\n"); // Display new coordinator name
                } else if (line.startsWith("WELCOME")) {
                    // Inform new client of coordinator information
                    String coordinatorName = line.substring(8); // Extract coordinator name
                    messageArea.append("The current coordinator is: " + coordinatorName + "\n"); // Display coordinator name
                } else if (line.startsWith("NAMES")) {
                	// Handle list of names message
                	String namesMessage = line.substring(6); // Extract names message
                    String[] namesArray = namesMessage.split(", "); // Split names into an array
                    // Append each name to the messageArea
                    for (String name : namesArray) {
                        messageArea.append(name + "\n");}
                }
            }
        } finally {
            // Close resources when done or on error
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    // Main method to start the chat client
    public static void main(String[] args) throws Exception {
        // Prompt for server address
        String serverAddress = JOptionPane.showInputDialog(
            null,
            "Enter the server address:",
            "Server address selection",
            JOptionPane.PLAIN_MESSAGE
        );
        // Prompt for server port
        int port = Integer.parseInt(JOptionPane.showInputDialog(
            null,
            "Enter the server port:",
            "Port selection",
            JOptionPane.PLAIN_MESSAGE
        ));
        // Prompt for username
        String username = JOptionPane.showInputDialog(
            null,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE
        );

        // Create User object with provided values
        User user = new User(serverAddress, port, username);

        // Create new ChatClient instance and setup GUI
        ChatClient chatClient = new ChatClient(user);
        chatClient.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure application exits on close
        chatClient.frame.setVisible(true); // Show the application window
        chatClient.run(); // Start the chat client
    }
}
