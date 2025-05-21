package model;

import java.util.UUID;

public class AuthData {
    private String authToken;
    private String username;

    public AuthData(String username) {
        this.username = username;
        generateAuthToken();
    }

    public void generateAuthToken() {
        authToken = UUID.randomUUID().toString();
    }

    public String getUsername() {
        return username;
    }

    public String getAuthToken() {
        return authToken;
    }
}
