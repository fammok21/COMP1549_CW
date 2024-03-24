package coursework_files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CoordinatorTest {
    private Coordinator coordinator;

    @BeforeEach
    void setUp() {
        // Initialize a Coordinator instance with a mock User before each test
        User user = new User("localhost", 8000, "testCoordinator");
        coordinator = new Coordinator(user);
    }

    @Test
    void testReturnCoordinatorDetails() {
        // Test that returnCoordinator returns the expected string representation of the coordinator's details
        String expected = "testCoordinatorlocalhost8000";
        String actual = coordinator.returnCoordinator();
        assertEquals(expected, actual, "The returnCoordinator method should return the concatenated username, server address, and port.");
    }

    @Test
    void testReturnUserDetails() {
        // Test that returnUserDetails simply returns the list passed to it
        List<String> names = Arrays.asList("user1", "user2", "user3");
        List<String> result = coordinator.returnUserDetails(names);
        assertEquals(names, result, "The returnUserDetails method should return the same list of names it was given.");
    }   
}
