package cwFinal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerJTest {
    
    private static Thread serverThread;

    @BeforeAll
    public static void setup() {
        // Start the ChatServer in a separate thread to not block the test execution
        serverThread = new Thread(() -> {
            try {
                String[] args = {};
                Server.main(args); // Start the server
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
        try {
            Thread.sleep(2000); // Wait a bit for the server to start up
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void tearDown() {
        serverThread.interrupt(); // Attempt to stop the server after tests
    }

    @Test
    public void testConnectionToServer() {
        try {
            // Attempt to connect to the server
            Socket socket = new Socket("localhost", 59001);
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Check if server asks for name submission
            assertTrue(in.hasNextLine());
            String line = in.nextLine();
            assertTrue(line.contains("SUBMITNAME"));

            // Submit a name and check for acceptance
            out.println("testUser");
            assertTrue(in.hasNextLine());
            line = in.nextLine();
            assertTrue(line.contains("NAMEACCEPTED"));

            // Cleanup
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            fail("Failed to connect to the server: " + e.getMessage());
        }
}
}
