package week3; 

public class User {
    private String serverAddress;
    private int port;
    private String username;

    // Constructor
    public User(String serverAddress, int port, String username) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
    }

    // Getters
    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    // Builder Class
    public static class UserBuilder {
        private String serverAddress;
        private int port;
        private String username;

        // Constructor with required fields
        public UserBuilder(String serverAddress, int port, String username) {
            this.serverAddress = serverAddress;
            this.port = port;
            this.username = username;
        }

        // Setters for optional fields
        public UserBuilder setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public UserBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        // Build method to construct User object
        public User build() {
            return new User(serverAddress, port, username);
        }
    }
}
