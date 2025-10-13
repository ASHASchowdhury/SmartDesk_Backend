package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.AuthRequestDTO;
import com.OfficeManagement.OfficeProject.dtos.AuthResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://10.0.6.1:3000"})
public class AuthController {

    // Handle user login - check credentials and return response
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO loginRequest) {
        System.out.println("Login attempt: " + loginRequest.getUsername());

        // Check if username and password match our hardcoded users
        if (isValidUser(loginRequest.getUsername(), loginRequest.getPassword())) {
            // Get the role for this user and return success
            String role = getUserRole(loginRequest.getUsername());
            return new AuthResponseDTO(true, "Login successful", loginRequest.getUsername(), role);
        } else {
            // Return failure if credentials don't match
            return new AuthResponseDTO(false, "Invalid credentials", null, null);
        }
    }

    // Hardcoded user validation - in real app, check against database
    private boolean isValidUser(String username, String password) {
        return (username.equals("hr") && password.equals("hr123")) ||
                (username.equals("director") && password.equals("director123")) ||
                (username.equals("pm") && password.equals("pm123")) ||
                (username.equals("cto") && password.equals("cto123"));
    }

    // Figure out what role this user has based on their username
    private String getUserRole(String username) {
        switch (username) {
            case "hr": return "HR";
            case "director": return "DIRECTOR";
            case "pm": return "PROJECT_MANAGER";
            case "cto": return "CTO";
            default: return "USER"; // Just in case we get an unknown user
        }
    }
}