package cwFinal;

public class User {
    private String serverAddress; // Holds the server address the user connects to
    private int port; // Holds the port number for the server connection
    private String username; // Holds the username of the user

    // Constructor with input validation for fault tolerance
    // Ensures the user object is always in a valid state
    public User(String serverAddress, int port, String username) {
        // Validate server address (Fault Tolerance)
        if (serverAddress == null || serverAddress.isEmpty()) {
            throw new IllegalArgumentException("Server address cannot be null or empty");
        }
        // Validate port number (Fault Tolerance)
        if (port <= 0) {
            throw new IllegalArgumentException("Port number must be positive");
        }
        // Validate username (Fault Tolerance)
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
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

    // Static inner Builder class for User, implementing the Builder pattern.
    // Allows for fluent setting of User properties.
    public static class UserBuilder {
        private String serverAddress; // Intended server address
        private int port; // Intended port number
        private String username; // Intended username

        // Constructor for the Builder with initial values
        public UserBuilder(String serverAddress, int port, String username) {
            this.serverAddress = serverAddress;
            this.port = port;
            this.username = username;
        }

        // Setter for server address with input validation (Fault Tolerance)
        public UserBuilder setServerAddress(String serverAddress) {
            if (serverAddress == null || serverAddress.isEmpty()) {
                throw new IllegalArgumentException("Server address cannot be null or empty");
            }
            this.serverAddress = serverAddress;
            return this; // Return the builder for chaining
        }

        // Setter for port with input validation (Fault Tolerance)
        public UserBuilder setPort(int port) {
            if (port <= 0) {
                throw new IllegalArgumentException("Port number must be positive");
            }
            this.port = port;
            return this; // Return the builder for chaining
        }

        // Setter for username with input validation (Fault Tolerance)
        public UserBuilder setUsername(String username) {
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            this.username = username;
            return this; // Return the builder for chaining
        }

        // Build method constructs a User object using the builder's values
        // This step also inherently applies the validation logic before creating the User object
        public User build() {
            return new User(serverAddress, port, username);
        }
    }
}

