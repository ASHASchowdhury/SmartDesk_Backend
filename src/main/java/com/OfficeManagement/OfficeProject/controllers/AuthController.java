package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.AuthRequestDTO;
import com.OfficeManagement.OfficeProject.dtos.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://10.0.6.1:3000"})
public class AuthController {

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO loginRequest) {
        System.out.println("Login attempt: " + loginRequest.getUsername());

        // Validate input
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return new AuthResponseDTO(false, "Username is required", null, null);
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return new AuthResponseDTO(false, "Password is required", null, null);
        }

        if (isValidUser(loginRequest.getUsername(), loginRequest.getPassword())) {
            String role = getUserRole(loginRequest.getUsername());
            return new AuthResponseDTO(true, "Login successful", loginRequest.getUsername(), role);
        } else {
            return new AuthResponseDTO(false, "Invalid credentials", null, null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // In a real application, you would invalidate the token/session
        return ResponseEntity.ok("{\"message\": \"Logout successful\"}");
    }

    private boolean isValidUser(String username, String password) {
        return (username.equals("hr") && password.equals("hr123")) ||
                (username.equals("director") && password.equals("director123")) ||
                (username.equals("pm") && password.equals("pm123")) ||
                (username.equals("cto") && password.equals("cto123")) ||
                (username.equals("employee") && password.equals("employee123"));
    }

    private String getUserRole(String username) {
        switch (username) {
            case "hr": return "HR";
            case "director": return "DIRECTOR";
            case "pm": return "PROJECT_MANAGER";
            case "cto": return "CTO";
            case "employee": return "USER";
            default: return "USER";
        }
    }
}