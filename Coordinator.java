package week3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
public class Coordinator{
    User user;

    public Coordinator(User user) {
    	this.user = user;
    }
    
    public String returnCoordinator() {
    	return user.getUsername() + user.getServerAddress() + user.getPort();
    }
    
    
    public void checkClients(List<String> names) {
    	while (true) {
            // Perform the necessary checks on the active group members
            // For example, you can print the current state of active group members
            System.out.println("Active Group Members: " + names);

            try {
                // Sleep for 20 seconds before the next check
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }}
    
    public List<String> returnUserDetails(List<String> names){
    	return names;
    }}
