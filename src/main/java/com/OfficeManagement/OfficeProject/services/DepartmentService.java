package com.OfficeManagement.OfficeProject.services;

import com.OfficeManagement.OfficeProject.dtos.DepartmentDTO;
import java.util.List;

public interface DepartmentService {
    DepartmentDTO saveDepartment(DepartmentDTO departmentDTO);
    List<DepartmentDTO> getAllDepartment();
    DepartmentDTO getDepartmentById(Long id);
    DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
}