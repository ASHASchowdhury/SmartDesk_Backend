package com.OfficeManagement.OfficeProject.dtos;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private String content;
    private String senderRole;    // Only role, no username
    private LocalDateTime timestamp;
    private Long roomId;

    // Constructors
    public ChatMessageDTO() {}

    public ChatMessageDTO(String content, String senderRole, Long roomId) {
        this.content = content;
        this.senderRole = senderRole;
        this.roomId = roomId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
}