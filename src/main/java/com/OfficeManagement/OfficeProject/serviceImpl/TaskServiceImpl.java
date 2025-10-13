package com.OfficeManagement.OfficeProject.serviceImpl;

import com.OfficeManagement.OfficeProject.dtos.TaskRequestDTO;
import com.OfficeManagement.OfficeProject.dtos.TaskResponseDTO;
import com.OfficeManagement.OfficeProject.models.Task;
import com.OfficeManagement.OfficeProject.repository.TaskRepository;
import com.OfficeManagement.OfficeProject.services.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Convert TaskRequestDTO to Task entity
    private Task convertToEntity(TaskRequestDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setDueDate(LocalDate.parse(dto.getDueDate()));
        task.setEstimatedHours(dto.getEstimatedHours());
        task.setAssignedToId(dto.getAssignedToId());
        task.setStatus("PENDING");

        // CHANGED: Handle Boolean fields safely
        task.setStarted(dto.getStarted() != null ? dto.getStarted() : false);
        task.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : false);

        return task;
    }

    // Convert Task entity to TaskResponseDTO
    private TaskResponseDTO convertToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate().toString());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setAssignedToId(task.getAssignedToId());
        dto.setDeleted(task.isDeleted());

        // CHANGED: Use Boolean getters
        dto.setStarted(task.getStarted());
        dto.setCompleted(task.getCompleted());

        return dto;
    }

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        // Validate required fields
        if (taskRequestDTO.getTitle() == null || taskRequestDTO.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Task title is required");
        }
        if (taskRequestDTO.getAssignedToId() == null) {
            throw new RuntimeException("Assignee ID is required");
        }

        Task task = convertToEntity(taskRequestDTO);
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Override
    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findByDeletedFalse()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
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
            existingTask.setDueDate(LocalDate.parse(taskRequestDTO.getDueDate()));
        }
        if (taskRequestDTO.getEstimatedHours() != null) {
            existingTask.setEstimatedHours(taskRequestDTO.getEstimatedHours());
        }
        if (taskRequestDTO.getAssignedToId() != null) {
            existingTask.setAssignedToId(taskRequestDTO.getAssignedToId());
        }

        // CHANGED: Update Boolean fields
        if (taskRequestDTO.getStarted() != null) {
            existingTask.setStarted(taskRequestDTO.getStarted());
        }
        if (taskRequestDTO.getCompleted() != null) {
            existingTask.setCompleted(taskRequestDTO.getCompleted());
        }

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setDeleted(true);
        taskRepository.save(task);
    }

    @Override
    public List<TaskResponseDTO> getTasksByAssignee(Long assigneeId) {
        return taskRepository.findByAssignedToIdAndDeletedFalse(assigneeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        // Validate status
        if (!isValidStatus(status)) {
            throw new RuntimeException("Invalid status: " + status);
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Override
    public List<TaskResponseDTO> getTasksByPriority(String priority) {
        return taskRepository.findByPriorityAndDeletedFalse(priority)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> getOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByDeletedFalse()
                .stream()
                .filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isBefore(today) &&
                        !"COMPLETED".equals(task.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return List.of("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED").contains(status);
    }
}