// src/main/java/com/OfficeManagement/OfficeProject/controllers/EmployeeController.java
package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import com.OfficeManagement.OfficeProject.dtos.UserContext;
import com.OfficeManagement.OfficeProject.services.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        String role = UserContext.getCurrentRole();
        System.out.println("🔍 CREATE EMPLOYEE - Current role: " + role);

        // Remove ROLE_ prefix for comparison
        String simpleRole = role.replace("ROLE_", "");

        if (!simpleRole.equals("HR") && !simpleRole.equals("DIRECTOR") && !simpleRole.equals("CTO")) {
            return ResponseEntity.status(403).body("Access denied: Insufficient permissions");
        }

        try {
            EmployeeDTO savedEmployee = employeeService.saveEmployee(employeeDTO);
            return ResponseEntity.ok("Employee created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        String role = UserContext.getCurrentRole();

        // Regular users can only access their own profile through /profile/me
        if (role.equals("ROLE_USER")) {
            return ResponseEntity.status(403).body("Access denied: Use /profile/me to view your profile");
        }

        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployee() {
        String role = UserContext.getCurrentRole();

        // FIX: Allow CTO to see all employees
        if (role.equals("ROLE_USER")) {
            return ResponseEntity.status(403).body("Access denied: Insufficient permissions");
        }

        List<EmployeeDTO> employees = employeeService.getAllEmployee();
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO employeeDTO) {
        // Check if user has permission to update employees
        String role = UserContext.getCurrentRole();

        // FIX: Added ROLE_CTO to the permission check
        if (!role.equals("ROLE_HR") && !role.equals("ROLE_DIRECTOR") && !role.equals("ROLE_CTO")) {
            return ResponseEntity.status(403).body("Access denied: Insufficient permissions");
        }

        try {
            EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
            return ResponseEntity.ok("Employee updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        // Check if user has permission to delete employees
        String role = UserContext.getCurrentRole();

        // FIX: Added ROLE_CTO to the permission check
        if (!role.equals("ROLE_HR") && !role.equals("ROLE_DIRECTOR") && !role.equals("ROLE_CTO")) {
            return ResponseEntity.status(403).body("Access denied: Insufficient permissions");
        }

        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}