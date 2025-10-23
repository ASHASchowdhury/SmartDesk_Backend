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
    public ChatMessageDTO sendMessage(ChatMessageDTO messageDTO, String senderRole) {
        // Create message with role from RoleBasedFilter
        ChatMessage message = new ChatMessage(
                messageDTO.getContent(),
                senderRole,        // Role from UserContext
                messageDTO.getRoomId()
        );

        // Save to database
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Convert to DTO
        ChatMessageDTO savedDTO = convertToDTO(savedMessage);

        // Send real-time update
        String topic = "/topic/chat/" + messageDTO.getRoomId();
        messagingTemplate.convertAndSend(topic, savedDTO);

        System.out.println("Message sent to room " + messageDTO.getRoomId() +
                " from role: " + senderRole);

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
        dto.setContent(message.getContent());
        dto.setRoomId(message.getRoomId());
        dto.setSenderRole(message.getSenderRole()); // Only role
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}