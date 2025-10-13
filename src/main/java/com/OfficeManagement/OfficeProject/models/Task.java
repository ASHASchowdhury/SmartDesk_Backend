package com.OfficeManagement.OfficeProject.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private LocalDate dueDate;
    private Integer estimatedHours;
    private Long assignedToId;
    private boolean deleted = false;
    private Boolean started = false;        // Changed from boolean to Boolean
    private Boolean completed = false;      // Changed from boolean to Boolean

    // Constructors
    public Task() {}

    public Task(String title, String description, String priority, LocalDate dueDate,
                Integer estimatedHours, Long assignedToId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedHours = estimatedHours;
        this.assignedToId = assignedToId;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public Boolean getStarted() { return started; }
    public void setStarted(Boolean started) { this.started = started; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}