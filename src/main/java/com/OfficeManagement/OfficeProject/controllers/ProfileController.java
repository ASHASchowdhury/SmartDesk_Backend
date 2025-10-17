package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import com.OfficeManagement.OfficeProject.dtos.UserContext;
import com.OfficeManagement.OfficeProject.services.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    private final EmployeeService employeeService;

    public ProfileController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        try {
            String currentUsername = UserContext.getCurrentUsername();
            String currentRole = UserContext.getCurrentRole();

            System.out.println("Fetching profile for: " + currentUsername + " with role: " + currentRole);

            if (currentUsername == null || "anonymous".equals(currentUsername)) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            // In a real implementation, you would fetch by username/email
            // For demo, we'll create a mock profile based on the username
            EmployeeDTO profile = createMockProfile(currentUsername, currentRole);

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching profile: " + e.getMessage());
        }
    }

    private EmployeeDTO createMockProfile(String username, String role) {
        // Create a mock profile based on username
        EmployeeDTO profile = new EmployeeDTO();
        profile.setId(1L);
        profile.setName(username.toUpperCase() + " User");
        profile.setEmail(username + "@company.com");
        profile.setPhoneNumber("+1234567890");
        profile.setGender("Prefer not to say");
        profile.setActive(true);
        profile.setBloodGroup("O+");

        return profile;
    }
}