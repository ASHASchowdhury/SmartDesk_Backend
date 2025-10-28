package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import com.OfficeManagement.OfficeProject.dtos.UserContext;
import com.OfficeManagement.OfficeProject.services.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile() {
        try {
            String currentUsername = UserContext.getCurrentUsername();
            String currentRole = UserContext.getCurrentRole();

            System.out.println("Fetching profile for: " + currentUsername + " with role: " + currentRole);

            if (currentUsername == null || "anonymous".equals(currentUsername) || currentUsername.trim().isEmpty()) {
                return ResponseEntity.status(401).body("{\"error\": \"User not authenticated\"}");
            }

            // Try to get actual employee profile
            try {
                EmployeeDTO profile = employeeService.getEmployeeByUsername(currentUsername);
                return ResponseEntity.ok(profile);
            } catch (RuntimeException e) {
                // Fallback: create mock profile if employee not found in database
                System.out.println("Employee not found in database, creating mock profile");
                EmployeeDTO mockProfile = createMockProfile(currentUsername, currentRole);
                return ResponseEntity.ok(mockProfile);
            }

        } catch (Exception e) {
            System.err.println("Error fetching profile: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"Error fetching profile: " + e.getMessage() + "\"}");
        }
    }

    private EmployeeDTO createMockProfile(String username, String role) {
        EmployeeDTO profile = new EmployeeDTO();
        profile.setId(1L);
        profile.setName(username.toUpperCase() + " User");
        profile.setEmail(username + "@company.com");
        profile.setPhoneNumber("+1234567890");
        profile.setGender("Prefer not to say");
        profile.setActive(true);
        profile.setBloodGroup("O+");
        // Set a default department to avoid null pointer
        profile.setDepartmentDTO(new com.OfficeManagement.OfficeProject.dtos.DepartmentDTO(1L, "Default Department"));

        return profile;
    }
}