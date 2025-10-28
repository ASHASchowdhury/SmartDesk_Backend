// DepartmentController.java - Added proper authorization
package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.DepartmentDTO;
import com.OfficeManagement.OfficeProject.services.DepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "http://localhost:3000")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO')")
    public DepartmentDTO saveDepartment(@RequestBody DepartmentDTO departmentDTO){
        return departmentService.saveDepartment(departmentDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO', 'PROJECT_MANAGER')")
    public DepartmentDTO getDepartmentById(@PathVariable Long id){
        return departmentService.getDepartmentById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO', 'PROJECT_MANAGER')")
    public List<DepartmentDTO> getAllDepartment(){
        return departmentService.getAllDepartment();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO')")
    public DepartmentDTO updateDepartment(@PathVariable Long id, @RequestBody DepartmentDTO departmentDTO){
        return departmentService.updateDepartment(id, departmentDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'CTO')")
    public void deleteDepartment(@PathVariable Long id){
        departmentService.deleteDepartment(id);
    }
}