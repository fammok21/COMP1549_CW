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

public class ChatClient {
    User user; // User object to store user information
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter"); // JFrame for the chat client window
    JTextField textField = new JTextField(50); // TextField for entering messages
    JTextArea messageArea = new JTextArea(16, 50); // TextArea for displaying messages

    // Constructor to initialize the ChatClient
    public ChatClient(User user) {
        this.user = user;

        // Setting up the GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // Action listener for the TextField to send messages
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText()); // Send message to server
                textField.setText(""); // Clear TextField after sending message
            }
        });
    }

    // Method to run the chat client
    private void run() throws IOException {
        try {
            Socket socket = new Socket(user.getServerAddress(), user.getPort()); // Connect to server
            in = new Scanner(socket.getInputStream()); // Create scanner to read from server
            out = new PrintWriter(socket.getOutputStream(), true); // Create PrintWriter to write to server
            out.println(user.getUsername()); // Send username to server

            // Main loop to receive messages from server
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("NAMEACCEPTED")) {
                    frame.setTitle("Chatter - " + user.getUsername()); // Set frame title with username
                    textField.setEditable(true); // Enable TextField for sending messages
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n"); // Append message to messageArea
                } else if (line.startsWith("COORDINATOR")) {
                    // Handle coordinator message
                    String coordinatorName = line.substring(12); // Extract coordinator name from the message
                    // Update UI or perform any other necessary action
                    // For example, you can display a message indicating who the coordinator is
                    messageArea.append("The coordinator is now: " + coordinatorName + "\n");
                } else if (line.startsWith("NEW_COORDINATOR")) {
                    // Handle new coordinator message
                    String newCoordinatorName = line.substring(16); // Extract new coordinator name from the message
                    // Update UI or perform any other necessary action
                    // For example, you can display a message indicating who the new coordinator is
                    messageArea.append("A new coordinator has been appointed: " + newCoordinatorName + "\n");
                } else if (line.startsWith("WELCOME")) {
                    // Inform new client of coordinator information
                    String coordinatorName = line.substring(8);
                    messageArea.append("The current coordinator is: " + coordinatorName + "\n");
                } else if (line.startsWith("NAMES")) {
                	String namesMessage = line.substring(6); // Assuming "NAMES " prefix is 6 characters long
                    String[] namesArray = namesMessage.split(", "); // Split the names using the comma and space delimiter
                    // Append each name to the messageArea
                    for (String name : namesArray) {
                        messageArea.append(name + "\n");}
                }
            }
        } finally {
            // Close resources (socket, scanner, PrintWriter) when done
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }


    // Main method to start the chat client
    public static void main(String[] args) throws Exception {
        String serverAddress = JOptionPane.showInputDialog(
            null,
            "Enter the server address:",
            "Server address selection",
            JOptionPane.PLAIN_MESSAGE
        );
        int port = Integer.parseInt(JOptionPane.showInputDialog(
            null,
            "Enter the server port:",
            "Port selection",
            JOptionPane.PLAIN_MESSAGE
        ));
        String username = JOptionPane.showInputDialog(
            null,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE
        );

        // Create User object with provided values
        User user = new User(serverAddress, port, username);

        // Create new ChatClient instance with the User object
        ChatClient chatClient = new ChatClient(user);
        chatClient.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation for frame
        chatClient.frame.setVisible(true); // Make frame visible
        chatClient.run(); // Run the chat client
    }
}
