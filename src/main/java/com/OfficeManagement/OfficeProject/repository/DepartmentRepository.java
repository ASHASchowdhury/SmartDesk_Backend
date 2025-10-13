package com.OfficeManagement.OfficeProject.repository;

import com.OfficeManagement.OfficeProject.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByOrderById();
}