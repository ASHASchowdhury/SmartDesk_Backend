package com.OfficeManagement.OfficeProject.dtos;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private String content;
    private String senderRole;
    private String senderName;
    private LocalDateTime timestamp;
    private Long roomId;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String content, String senderRole, String senderName, Long roomId) {
        this.content = content;
        this.senderRole = senderRole;
        this.senderName = senderName;
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

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
}