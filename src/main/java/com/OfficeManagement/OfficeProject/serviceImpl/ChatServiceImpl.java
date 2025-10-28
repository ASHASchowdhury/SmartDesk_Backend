package com.OfficeManagement.OfficeProject.serviceImpl;

import com.OfficeManagement.OfficeProject.dtos.ChatMessageDTO;
import com.OfficeManagement.OfficeProject.models.ChatMessage;
import com.OfficeManagement.OfficeProject.repository.ChatMessageRepository;
import com.OfficeManagement.OfficeProject.services.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(ChatMessageRepository chatMessageRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public ChatMessageDTO sendMessage(ChatMessageDTO messageDTO, String senderRole, String senderName) {
        // Validate input
        if (messageDTO == null) {
            throw new RuntimeException("Message data cannot be null");
        }
        if (messageDTO.getRoomId() == null) {
            throw new RuntimeException("Room ID is required");
        }

        // Determine message type
        String messageType = (messageDTO.getImageUrl() != null && !messageDTO.getImageUrl().isEmpty()) ? "IMAGE" : "TEXT";

        // Handle empty text messages for image-only messages
        String content = messageDTO.getContent() != null ? messageDTO.getContent() : "";
        if (content.isEmpty() && "TEXT".equals(messageType)) {
            throw new RuntimeException("Message content cannot be empty for text messages");
        }

        // Handle null values safely
        String imageUrl = messageDTO.getImageUrl() != null ? messageDTO.getImageUrl() : null;
        String imageName = messageDTO.getImageName() != null ? messageDTO.getImageName() : null;

        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setSenderRole(senderRole != null ? senderRole : "USER");
        message.setSenderName(senderName != null ? senderName : "Unknown User");
        message.setRoomId(messageDTO.getRoomId());
        message.setImageUrl(imageUrl);
        message.setImageName(imageName);
        message.setMessageType(messageType);
        message.setTimestamp(java.time.LocalDateTime.now());

        try {
            ChatMessage savedMessage = chatMessageRepository.save(message);
            ChatMessageDTO savedDTO = convertToDTO(savedMessage);

            String topic = "/topic/chat/" + messageDTO.getRoomId();
            messagingTemplate.convertAndSend(topic, savedDTO);

            return savedDTO;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getRoomMessages(Long roomId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        try {
            List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId);
            return messages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get room messages: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getRecentMessages(Long roomId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        try {
            List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId);
            return messages.stream()
                    .limit(50)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get recent messages: " + e.getMessage());
        }
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        if (message == null) {
            return null;
        }

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent() != null ? message.getContent() : "");
        dto.setRoomId(message.getRoomId());
        dto.setSenderRole(message.getSenderRole());
        dto.setSenderName(message.getSenderName());
        dto.setTimestamp(message.getTimestamp());
        dto.setImageUrl(message.getImageUrl());
        dto.setImageName(message.getImageName());
        dto.setMessageType(message.getMessageType() != null ? message.getMessageType() : "TEXT");
        return dto;
    }
}