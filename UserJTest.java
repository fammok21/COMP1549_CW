package week3;

import org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testValidUserCreation() {
        // Test the creation of a User object with valid parameters
        String serverAddress = "127.0.0.1";
        int port = 8080;
        String username = "testUser";

        User user = new User(serverAddress, port, username);

        assertNotNull(user);
        assertEquals(serverAddress, user.getServerAddress());
        assertEquals(port, user.getPort());
        assertEquals(username, user.getUsername());
    }

    @Test
    void testUserCreationWithInvalidServerAddress() {
        // Test User creation with an invalid server address
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("", 8080, "testUser");
        });

        String expectedMessage = "Server address cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUserCreationWithInvalidPort() {
        // Test User creation with an invalid port number
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("127.0.0.1", -1, "testUser");
        });

        String expectedMessage = "Port number must be positive";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUserCreationWithInvalidUsername() {
        // Test User creation with an invalid username
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("127.0.0.1", 8080, "");
        });

        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUserBuilderWithValidInputs() {
        // Test the UserBuilder with valid inputs
        User user = new User.UserBuilder("127.0.0.1", 8080, "builderUser").build();

        assertNotNull(user);
        assertEquals("127.0.0.1", user.getServerAddress());
        assertEquals(8080, user.getPort());
        assertEquals("builderUser", user.getUsername());
    }

   
}

