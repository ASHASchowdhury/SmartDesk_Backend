package com.OfficeManagement.OfficeProject.serviceImpl;

import com.OfficeManagement.OfficeProject.dtos.DepartmentDTO;
import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import com.OfficeManagement.OfficeProject.models.Department;
import com.OfficeManagement.OfficeProject.models.Employee;
import com.OfficeManagement.OfficeProject.repository.DepartmentRepository;
import com.OfficeManagement.OfficeProject.repository.EmployeeRepository;
import com.OfficeManagement.OfficeProject.services.EmployeeService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        DepartmentDTO departmentDTO = null;
        if (employee.getDepartment() != null) {
            Department department = employee.getDepartment();
            departmentDTO = new DepartmentDTO();
            departmentDTO.setId(department.getId());
            departmentDTO.setDeptId(department.getDeptId());
            departmentDTO.setName(department.getName());
            departmentDTO.setDescription(department.getDescription());
        }

        return new EmployeeDTO(
                employee.getId(),
                employee.getName(),
                employee.getPhoneNumber(),
                employee.getEmail(),
                employee.getGender(),
                employee.isActive(),
                employee.getBloodGroup(),
                employee.getDateOfBirth(),
                departmentDTO
        );
    }

    private Employee convertToEntity(EmployeeDTO employeeDTO) {
        if (employeeDTO == null) {
            throw new RuntimeException("Employee data cannot be null");
        }

        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        employee.setEmail(employeeDTO.getEmail());
        employee.setGender(employeeDTO.getGender());
        employee.setActive(employeeDTO.isActive());
        employee.setBloodGroup(employeeDTO.getBloodGroup());
        employee.setDateOfBirth(employeeDTO.getDateOfBirth());

        // Map department safely
        if (employeeDTO.getDepartmentDTO() != null && employeeDTO.getDepartmentDTO().getId() != null) {
            Department dept = departmentRepository.findById(employeeDTO.getDepartmentDTO().getId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + employeeDTO.getDepartmentDTO().getId()));
            employee.setDepartment(dept);
        } else {
            throw new RuntimeException("Department must be provided");
        }

        return employee;
    }

    private void validateEmployeeDTO(EmployeeDTO employeeDTO) {
        if (employeeDTO == null) {
            throw new RuntimeException("Employee data cannot be null");
        }
        if (employeeDTO.getName() == null || employeeDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("Employee name is required");
        }
        if (employeeDTO.getEmail() == null || employeeDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Employee email is required");
        }
        if (employeeDTO.getPhoneNumber() == null || employeeDTO.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Employee phone number is required");
        }
        if (employeeDTO.getDepartmentDTO() == null || employeeDTO.getDepartmentDTO().getId() == null) {
            throw new RuntimeException("Department is required");
        }

        // Validate email format
        if (!isValidEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @Override
    public EmployeeDTO saveEmployee(EmployeeDTO employeeDTO) {
        validateEmployeeDTO(employeeDTO);

        // Check for duplicate email
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + employeeDTO.getEmail());
        }

        // Check for duplicate phone number
        if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists: " + employeeDTO.getPhoneNumber());
        }

        try {
            System.out.println("Creating employee: " + employeeDTO.getName());
            Employee saved = employeeRepository.save(convertToEntity(employeeDTO));
            System.out.println("Employee saved with id: " + saved.getId());
            return convertToDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save employee: " + e.getMessage());
        }
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        validateEmployeeDTO(employeeDTO);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Check for duplicate email (if changed)
        if (!existingEmployee.getEmail().equals(employeeDTO.getEmail()) &&
                employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + employeeDTO.getEmail());
        }

        // Check for duplicate phone number (if changed)
        if (!existingEmployee.getPhoneNumber().equals(employeeDTO.getPhoneNumber()) &&
                employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists: " + employeeDTO.getPhoneNumber());
        }

        // Update fields
        existingEmployee.setName(employeeDTO.getName());
        existingEmployee.setPhoneNumber(employeeDTO.getPhoneNumber());
        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setGender(employeeDTO.getGender());
        existingEmployee.setActive(employeeDTO.isActive());
        existingEmployee.setBloodGroup(employeeDTO.getBloodGroup());
        existingEmployee.setDateOfBirth(employeeDTO.getDateOfBirth());

        // Update department if provided
        if (employeeDTO.getDepartmentDTO() != null && employeeDTO.getDepartmentDTO().getId() != null) {
            Department dept = departmentRepository.findById(employeeDTO.getDepartmentDTO().getId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + employeeDTO.getDepartmentDTO().getId()));
            existingEmployee.setDepartment(dept);
        }

        try {
            Employee updated = employeeRepository.save(existingEmployee);
            return convertToDTO(updated);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update employee: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        try {
            // Check if employee exists
            if (!employeeRepository.existsById(id)) {
                throw new RuntimeException("Employee not found with id: " + id);
            }

            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

            System.out.println("Attempting to delete employee: " + employee.getName() + " (ID: " + id + ")");

            // Try to delete
            employeeRepository.deleteById(id);
            System.out.println("Employee deleted successfully: " + id);

        } catch (DataIntegrityViolationException e) {
            System.err.println("Cannot delete employee due to foreign key constraints: " + e.getMessage());
            throw new RuntimeException("Cannot delete employee: This employee is referenced in other records (e.g., tasks). Please remove those references first.");
        } catch (Exception e) {
            System.err.println("Error deleting employee " + id + ": " + e.getMessage());
            throw new RuntimeException("Error deleting employee: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployee() {
        try {
            return employeeRepository.findAllByOrderById()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get employees: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        try {
            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
            return convertToDTO(employee);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get employee: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("Username cannot be empty");
            }

            // Try to find by email (assuming username is email)
            Employee employee = employeeRepository.findByEmail(username);
            if (employee != null) {
                return convertToDTO(employee);
            }

            // Fallback: try to find by name (for demo purposes)
            List<Employee> employees = employeeRepository.findAll();
            Employee foundEmployee = employees.stream()
                    .filter(emp -> emp.getName() != null && emp.getName().equalsIgnoreCase(username))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Employee not found with username: " + username));

            return convertToDTO(foundEmployee);
        } catch (Exception e) {
            throw new RuntimeException("Error finding employee by username: " + e.getMessage());
        }
    }
}