package week3; 

// The User class represents a user in the chat application, encapsulating user details.
public class User {
    private String serverAddress; // Server address the user connects to
    private int port; // Port number for the server connection
    private String username; // Username of the user

    // Constructor initializes a new User with server address, port, and username
    public User(String serverAddress, int port, String username) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
    }

    // Getter for server address
    public String getServerAddress() {
        return serverAddress;
    }

    // Getter for port number
    public int getPort() {
        return port;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Static inner Builder class for User, following the Builder pattern for constructing User objects.
    public static class UserBuilder {
        private String serverAddress; // Server address
        private int port; // Port number
        private String username; // Username

        // Constructor for UserBuilder with required fields
        public UserBuilder(String serverAddress, int port, String username) {
            this.serverAddress = serverAddress;
            this.port = port;
            this.username = username;
        }

        // Setter for server address, returns the builder for chaining
        public UserBuilder setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        // Setter for port, returns the builder for chaining
        public UserBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        // Setter for username, returns the builder for chaining
        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        // Build method constructs a User object from the builder's values
        public User build() {
            return new User(serverAddress, port, username);
        }
    }
}

