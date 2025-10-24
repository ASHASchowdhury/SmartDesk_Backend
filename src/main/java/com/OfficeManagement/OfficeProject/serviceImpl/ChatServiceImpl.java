package com.OfficeManagement.OfficeProject.serviceImpl;

import com.OfficeManagement.OfficeProject.dtos.ChatMessageDTO;
import com.OfficeManagement.OfficeProject.models.ChatMessage;
import com.OfficeManagement.OfficeProject.repository.ChatMessageRepository;
import com.OfficeManagement.OfficeProject.services.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
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
        // Determine message type
        String messageType = (messageDTO.getImageUrl() != null && !messageDTO.getImageUrl().isEmpty()) ? "IMAGE" : "TEXT";

        // Handle null values safely
        String imageUrl = messageDTO.getImageUrl() != null ? messageDTO.getImageUrl() : null;
        String imageName = messageDTO.getImageName() != null ? messageDTO.getImageName() : null;
        String content = messageDTO.getContent() != null ? messageDTO.getContent() : "";

        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setSenderRole(senderRole);
        message.setSenderName(senderName);
        message.setRoomId(messageDTO.getRoomId());
        message.setImageUrl(imageUrl);
        message.setImageName(imageName);
        message.setMessageType(messageType);
        message.setTimestamp(java.time.LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);
        ChatMessageDTO savedDTO = convertToDTO(savedMessage);

        String topic = "/topic/chat/" + messageDTO.getRoomId();
        messagingTemplate.convertAndSend(topic, savedDTO);

        return savedDTO;
    }

    @Override
    public List<ChatMessageDTO> getRoomMessages(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getRecentMessages(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId);
        return messages.stream()
                .limit(50)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
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