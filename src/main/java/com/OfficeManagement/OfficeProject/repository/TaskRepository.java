package com.OfficeManagement.OfficeProject.repository;

import com.OfficeManagement.OfficeProject.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByDeletedFalse();
    List<Task> findByAssignedToIdAndDeletedFalse(Long assignedToId);
    List<Task> findByPriorityAndDeletedFalse(String priority);
    List<Task> findByStatusAndDeletedFalse(String status);
}