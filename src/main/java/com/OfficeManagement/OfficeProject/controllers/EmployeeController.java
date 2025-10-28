// EmployeeController.java - Fixed with proper role checks
package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import com.OfficeManagement.OfficeProject.dtos.UserContext;
import com.OfficeManagement.OfficeProject.services.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO')")
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();

        System.out.println("CREATE EMPLOYEE - User: " + username + ", Role: " + role);

        try {
            EmployeeDTO savedEmployee = employeeService.saveEmployee(employeeDTO);
            System.out.println("Employee created successfully by: " + username);
            return ResponseEntity.ok("Employee created successfully");
        } catch (RuntimeException e) {
            System.err.println("Error creating employee: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO', 'PROJECT_MANAGER')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();

        System.out.println("GET EMPLOYEE BY ID - User: " + username + ", Role: " + role + ", Employee ID: " + id);

        try {
            EmployeeDTO employee = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO', 'PROJECT_MANAGER')")
    public ResponseEntity<?> getAllEmployee() {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();

        System.out.println("GET ALL EMPLOYEES - User: " + username + ", Role: " + role);

        List<EmployeeDTO> employees = employeeService.getAllEmployee();
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO')")
    public ResponseEntity<String> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO employeeDTO) {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();

        System.out.println("UPDATE EMPLOYEE - User: " + username + ", Role: " + role + ", Employee ID: " + id);

        try {
            EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
            System.out.println("Employee updated successfully by: " + username);
            return ResponseEntity.ok("Employee updated successfully");
        } catch (RuntimeException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO')")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();

        System.out.println("DELETE EMPLOYEE - User: " + username + ", Role: " + role + ", Employee ID: " + id);

        try {
            employeeService.deleteEmployee(id);
            System.out.println("Employee deleted successfully: " + id);
            return ResponseEntity.ok("Employee deleted successfully");
        } catch (RuntimeException e) {
            System.err.println("Error deleting employee " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Error deleting employee: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error deleting employee " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}