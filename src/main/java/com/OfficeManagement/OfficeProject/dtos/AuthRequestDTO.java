package com.OfficeManagement.OfficeProject.dtos;

public class AuthRequestDTO {
    private String username;
    private String password;

    // Default empty constructor
    public AuthRequestDTO() {}

    // Constructor with username and password parameters
    public AuthRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Get the username
    public String getUsername() { return username; }
    // Set the username
    public void setUsername(String username) { this.username = username; }

    // Get the password
    public String getPassword() { return password; }
    // Set the password
    public void setPassword(String password) { this.password = password; }
}