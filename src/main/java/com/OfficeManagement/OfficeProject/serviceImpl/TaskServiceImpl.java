package com.OfficeManagement.OfficeProject.serviceImpl;

import com.OfficeManagement.OfficeProject.dtos.TaskRequestDTO;
import com.OfficeManagement.OfficeProject.dtos.TaskResponseDTO;
import com.OfficeManagement.OfficeProject.models.Task;
import com.OfficeManagement.OfficeProject.repository.TaskRepository;
import com.OfficeManagement.OfficeProject.services.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    private Task convertToEntity(TaskRequestDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Task data cannot be null");
        }

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());

        // Parse due date safely
        if (dto.getDueDate() != null) {
            try {
                task.setDueDate(LocalDate.parse(dto.getDueDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid due date format. Use YYYY-MM-DD");
            }
        }

        task.setEstimatedHours(dto.getEstimatedHours());
        task.setAssignedToId(dto.getAssignedToId());
        task.setStatus("PENDING");

        // Handle Boolean fields safely
        task.setStarted(dto.getStarted() != null ? dto.getStarted() : false);
        task.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : false);

        return task;
    }

    private TaskResponseDTO convertToDTO(Task task) {
        if (task == null) {
            return null;
        }

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate() != null ? task.getDueDate().toString() : null);
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setAssignedToId(task.getAssignedToId());
        dto.setDeleted(task.isDeleted());
        dto.setStarted(task.getStarted());
        dto.setCompleted(task.getCompleted());

        return dto;
    }

    private void validateTaskRequestDTO(TaskRequestDTO taskRequestDTO) {
        if (taskRequestDTO == null) {
            throw new RuntimeException("Task data cannot be null");
        }
        if (taskRequestDTO.getTitle() == null || taskRequestDTO.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Task title is required");
        }
        if (taskRequestDTO.getAssignedToId() == null) {
            throw new RuntimeException("Assignee ID is required");
        }
        if (taskRequestDTO.getDueDate() == null || taskRequestDTO.getDueDate().trim().isEmpty()) {
            throw new RuntimeException("Due date is required");
        }
    }

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        validateTaskRequestDTO(taskRequestDTO);

        try {
            Task task = convertToEntity(taskRequestDTO);
            Task savedTask = taskRepository.save(task);
            return convertToDTO(savedTask);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        try {
            return taskRepository.findByDeletedFalse()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        try {
            Task task = taskRepository.findById(id)
                    .filter(t -> !t.isDeleted())
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
            return convertToDTO(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get task: " + e.getMessage());
        }
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        if (taskRequestDTO == null) {
            throw new RuntimeException("Task data cannot be null");
        }

        try {
            Task existingTask = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

            // Update fields if provided
            if (taskRequestDTO.getTitle() != null) {
                existingTask.setTitle(taskRequestDTO.getTitle());
            }
            if (taskRequestDTO.getDescription() != null) {
                existingTask.setDescription(taskRequestDTO.getDescription());
            }
            if (taskRequestDTO.getPriority() != null) {
                existingTask.setPriority(taskRequestDTO.getPriority());
            }
            if (taskRequestDTO.getDueDate() != null) {
                try {
                    existingTask.setDueDate(LocalDate.parse(taskRequestDTO.getDueDate()));
                } catch (Exception e) {
                    throw new RuntimeException("Invalid due date format. Use YYYY-MM-DD");
                }
            }
            if (taskRequestDTO.getEstimatedHours() != null) {
                existingTask.setEstimatedHours(taskRequestDTO.getEstimatedHours());
            }
            if (taskRequestDTO.getAssignedToId() != null) {
                existingTask.setAssignedToId(taskRequestDTO.getAssignedToId());
            }

            // Update Boolean fields
            if (taskRequestDTO.getStarted() != null) {
                existingTask.setStarted(taskRequestDTO.getStarted());
            }
            if (taskRequestDTO.getCompleted() != null) {
                existingTask.setCompleted(taskRequestDTO.getCompleted());
                // Auto-update status if completed
                if (taskRequestDTO.getCompleted()) {
                    existingTask.setStatus("COMPLETED");
                }
            }

            Task updatedTask = taskRepository.save(existingTask);
            return convertToDTO(updatedTask);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task: " + e.getMessage());
        }
    }

    @Override
    public void deleteTask(Long id) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

            task.setDeleted(true);
            taskRepository.save(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByAssignee(Long assigneeId) {
        try {
            return taskRepository.findByAssignedToIdAndDeletedFalse(assigneeId)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks by assignee: " + e.getMessage());
        }
    }

    @Override
    public TaskResponseDTO updateTaskStatus(Long id, String status) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

            // Validate status
            if (!isValidStatus(status)) {
                throw new RuntimeException("Invalid status: " + status + ". Valid statuses: PENDING, IN_PROGRESS, COMPLETED, CANCELLED");
            }

            task.setStatus(status);

            // Auto-update started/completed based on status
            if ("IN_PROGRESS".equals(status)) {
                task.setStarted(true);
            } else if ("COMPLETED".equals(status)) {
                task.setCompleted(true);
                task.setStarted(true);
            }

            Task updatedTask = taskRepository.save(task);
            return convertToDTO(updatedTask);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task status: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByPriority(String priority) {
        try {
            return taskRepository.findByPriorityAndDeletedFalse(priority)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks by priority: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getOverdueTasks() {
        try {
            LocalDate today = LocalDate.now();
            return taskRepository.findByDeletedFalse()
                    .stream()
                    .filter(task -> task.getDueDate() != null &&
                            task.getDueDate().isBefore(today) &&
                            !"COMPLETED".equals(task.getStatus()))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get overdue tasks: " + e.getMessage());
        }
    }

    private boolean isValidStatus(String status) {
        return List.of("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED").contains(status);
    }
}