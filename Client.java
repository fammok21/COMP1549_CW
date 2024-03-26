package cwFinal;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
    User user;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);

    public Client(User user) {
        this.user = user;
        setupGUI();
        addActionListenerToSendMessages();
    }

    private void setupGUI() {
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();
    }

    private void addActionListenerToSendMessages() {
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    // Tries to establish a connection with the server and processes messages
    // Implements fault tolerance by handling exceptions that may occur during connection or message processing
    private void run() {
        try {
            establishConnection(); // Attempts to establish a connection with the server
            processServerMessages(); // Processes incoming messages from the server
        } catch (IOException e) {
            // Handles IO exceptions by displaying an error message to the user
            // Fault tolerance: informs the user about connection issues without crashing the application
            JOptionPane.showMessageDialog(frame, "Error connecting to the server: " + e.getMessage(),
                                          "Connection Error", JOptionPane.ERROR_MESSAGE);
        } catch (NoSuchElementException e) {
            // Handles exceptions when the server prematurely closes the connection
            // Fault tolerance: ensures a graceful shutdown and informs the user
            JOptionPane.showMessageDialog(frame, "Server closed the connection.",
                                          "Connection Lost", JOptionPane.WARNING_MESSAGE);
        } finally {
            // Ensures that resources are properly closed even if an exception occurs
            // Fault tolerance: resource management to prevent resource leaks
            closeResources();
            System.exit(0); // Ensures the application exits cleanly
        }
    }

    private void establishConnection() throws IOException {
        Socket socket = new Socket(user.getServerAddress(), user.getPort());
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(user.getUsername()); // Sends the username to the server
    }

    private void processServerMessages() {
        while (in.hasNextLine()) {
            String line = in.nextLine();
            processLine(line); // Processes each line received from the server
        }
    }

    private void processLine(String line) {
        // Generate a timestamp for the current message
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        // Check the type of message received from the server and act accordingly
        if (line.startsWith("NAMEACCEPTED")) {
            // The server has accepted the client's username
            frame.setTitle("Chatter - " + user.getUsername()); // Update the window title to include the username
            textField.setEditable(true); // Enable the text field to allow message input
        } else if (line.startsWith("MESSAGE")) {
            // A chat message to display
            messageArea.append("[" + timestamp + "] " + line.substring(8) + "\n"); // Append the message content to the chat display, skipping the "MESSAGE " prefix
        } else if (line.startsWith("COORDINATOR")) {
            // Coordinator status update
            String coordinatorName = line.substring(12); // Extract the coordinator's name
            messageArea.append("[" + timestamp + "] The coordinator is now: " + coordinatorName + "\n"); // Display the coordinator update
        } else if (line.startsWith("NEW_COORDINATOR")) {
            // A new coordinator has been appointed
            String newCoordinatorName = line.substring(16); // Extract the new coordinator's name
            messageArea.append("[" + timestamp + "] A new coordinator has been appointed: " + newCoordinatorName + "\n"); // Display the new coordinator announcement
        } else if (line.startsWith("WELCOME")) {
            // Welcome message for new clients, including the current coordinator's name
            String coordinatorName = line.substring(8); // Extract the coordinator's name from the message
            messageArea.append("[" + timestamp + "] The current coordinator is: " + coordinatorName + "\n"); // Display the current coordinator's name
        }

        // Scroll the message area to the end to make sure the latest messages are visible
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    
 // Closes scanner and print writer resources
    private void closeResources() {
        if (in != null) in.close();
        if (out != null) out.close();
    }

    public static void main(String[] args) {
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

        User user = new User(serverAddress, port, username);
        Client Client = new Client(user);
        Client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Client.frame.setVisible(true);
        Client.run();
    }
}
