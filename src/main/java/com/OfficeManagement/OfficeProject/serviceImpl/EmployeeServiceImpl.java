package com.OfficeManagement.OfficeProject.serviceImpl;

import com.OfficeManagement.OfficeProject.dtos.DepartmentDTO;
import com.OfficeManagement.OfficeProject.dtos.EmployeeDTO;
import com.OfficeManagement.OfficeProject.models.Department;
import com.OfficeManagement.OfficeProject.models.Employee;
import com.OfficeManagement.OfficeProject.repository.DepartmentRepository;
import com.OfficeManagement.OfficeProject.repository.EmployeeRepository;
import com.OfficeManagement.OfficeProject.services.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    // Convert Employee to EmployeeDTO
    private EmployeeDTO convertToDTO(Employee employee) {
        DepartmentDTO departmentDTO = null;
        if (employee != null && employee.getDepartment() != null) {
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

    // Convert EmployeeDTO to Employee entity
    private Employee convertToEntity(EmployeeDTO employeeDTO) {
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
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employee.setDepartment(dept);
        } else {
            throw new RuntimeException("Department must be provided");
        }

        return employee;
    }

    @Override
    public EmployeeDTO saveEmployee(EmployeeDTO employeeDTO) {
        // Validation
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        System.out.println("Creating employee: " + employeeDTO.getName());
        Employee saved = employeeRepository.save(convertToEntity(employeeDTO));
        System.out.println("Employee saved with id: " + saved.getId());
        return convertToDTO(saved);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee prsntEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Validation (only if email/phone is being changed)
        if (!prsntEmployee.getEmail().equals(employeeDTO.getEmail()) &&
                employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (!prsntEmployee.getPhoneNumber().equals(employeeDTO.getPhoneNumber()) &&
                employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        prsntEmployee.setName(employeeDTO.getName());
        prsntEmployee.setPhoneNumber(employeeDTO.getPhoneNumber());
        prsntEmployee.setEmail(employeeDTO.getEmail());
        prsntEmployee.setGender(employeeDTO.getGender());
        prsntEmployee.setActive(employeeDTO.isActive());
        prsntEmployee.setBloodGroup(employeeDTO.getBloodGroup());
        prsntEmployee.setDateOfBirth(employeeDTO.getDateOfBirth());

        if (employeeDTO.getDepartmentDTO() != null && employeeDTO.getDepartmentDTO().getId() != null) {
            Department dept = departmentRepository.findById(employeeDTO.getDepartmentDTO().getId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            prsntEmployee.setDepartment(dept);
        }

        Employee updated = employeeRepository.save(prsntEmployee);
        return convertToDTO(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public List<EmployeeDTO> getAllEmployee() {
        return employeeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToDTO(employee);
    }


    @Override
    public EmployeeDTO getEmployeeByUsername(String username) {
        // In a real app, you would query by username field
        // For demo, we'll find by email or return first employee
        try {
            Employee employee = employeeRepository.findByEmail(username);
            if (employee != null) {
                return convertToDTO(employee);
            }

            // Fallback: return first employee
            List<Employee> employees = employeeRepository.findAll();
            if (!employees.isEmpty()) {
                return convertToDTO(employees.get(0));
            }

            throw new RuntimeException("No employees found");
        } catch (Exception e) {
            throw new RuntimeException("Error finding employee by username: " + e.getMessage());
        }
    }
}