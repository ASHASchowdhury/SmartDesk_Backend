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
        ChatMessage message = new ChatMessage(
                messageDTO.getContent(),
                senderRole,
                senderName,
                messageDTO.getRoomId()
        );

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
        dto.setContent(message.getContent());
        dto.setRoomId(message.getRoomId());
        dto.setSenderRole(message.getSenderRole());
        dto.setSenderName(message.getSenderName());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}