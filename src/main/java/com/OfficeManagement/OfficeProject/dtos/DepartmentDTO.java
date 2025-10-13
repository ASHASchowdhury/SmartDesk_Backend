package com.OfficeManagement.OfficeProject.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class DepartmentDTO {
    private Long id;
    private String deptId;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime createdDate;
    private List<String> employeeNames;

    // Constructors
    public DepartmentDTO() {}

    public DepartmentDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public DepartmentDTO(Long id, String deptId, String name, String description) {
        this.id = id;
        this.deptId = deptId;
        this.name = name;
        this.description = description;
    }

    public DepartmentDTO(Long id, String deptId, String name, String description,
                         String createdBy, LocalDateTime createdDate, List<String> employeeNames) {
        this.id = id;
        this.deptId = deptId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.employeeNames = employeeNames;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<String> getEmployeeNames() { return employeeNames; }
    public void setEmployeeNames(List<String> employeeNames) { this.employeeNames = employeeNames; }
}