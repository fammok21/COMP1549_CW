package cwFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

// The Coordinator class represents the coordinator of the chat, responsible for managing chat operations.
public class Coordinator{
    User user; // User object representing the coordinator

    // Constructor initializes the Coordinator with a User object
    public Coordinator(User user) {
        this.user = user;
    }
    
    // Method to return a string representation of the coordinator's details
    public String returnCoordinator() {
        return user.getUsername() + user.getServerAddress() + user.getPort();
    }
    
    // Method to check the active clients periodically
    public void checkClients(List<String> names) {
        while (true) {
            // Perform the necessary checks on the active group members
            System.out.println("Active Group Members: " + names);

            try {
                TimeUnit.SECONDS.sleep(20); // Sleep for 20 seconds before the next check
            } catch (InterruptedException e) {
                e.printStackTrace(); // Handle interruption exceptions
            }
        }
    }
    
    // Method to return the list of user details, demonstrating encapsulation and potential future functionality
    public List<String> returnUserDetails(List<String> names){
        return names; // For now, simply returns the list passed to it
    }
}

    
    public List<String> returnUserDetails(List<String> names){
    	return names;
    }}
