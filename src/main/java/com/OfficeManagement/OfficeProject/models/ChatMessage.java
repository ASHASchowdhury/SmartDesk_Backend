package com.OfficeManagement.OfficeProject.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String senderRole;
    private String senderName;
    private LocalDateTime timestamp;
    private Long roomId;

    // ADD THESE FIELDS FOR IMAGE SUPPORT
    private String imageUrl;
    private String imageName;
    private String messageType; // "TEXT" or "IMAGE"

    public ChatMessage() {}

    // Updated constructor with ALL parameters
    public ChatMessage(String content, String senderRole, String senderName, Long roomId,
                       String imageUrl, String imageName, String messageType) {
        this.content = content;
        this.senderRole = senderRole;
        this.senderName = senderName;
        this.roomId = roomId;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.messageType = messageType;
        this.timestamp = LocalDateTime.now();
    }

    // Alternative constructor for text-only messages
    public ChatMessage(String content, String senderRole, String senderName, Long roomId) {
        this(content, senderRole, senderName, roomId, null, null, "TEXT");
    }

    // Getters and setters
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

    // Image getters and setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}