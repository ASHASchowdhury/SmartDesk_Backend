package com.OfficeManagement.OfficeProject.dtos;

public class TaskRequestDTO {
    private String title;
    private String description;
    private String priority;
    private String dueDate;
    private Integer estimatedHours;
    private Long assignedToId;
    private Boolean started;
    private Boolean completed;

    public TaskRequestDTO() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public Boolean getStarted() { return started; }
    public void setStarted(Boolean started) { this.started = started; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}