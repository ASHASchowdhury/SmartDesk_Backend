package com.OfficeManagement.OfficeProject.services;

import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeDTO saveEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    void deleteEmployee(Long id);
    List<EmployeeDTO> getAllEmployee();
    EmployeeDTO getEmployeeById(Long id);

    EmployeeDTO getEmployeeByUsername(String username);
}