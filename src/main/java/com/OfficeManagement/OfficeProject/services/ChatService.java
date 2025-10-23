package com.OfficeManagement.OfficeProject.services;

import com.OfficeManagement.OfficeProject.dtos.ChatMessageDTO;
import java.util.List;

public interface ChatService {
    ChatMessageDTO sendMessage(ChatMessageDTO messageDTO, String senderRole);
    List<ChatMessageDTO> getRoomMessages(Long roomId);
    List<ChatMessageDTO> getRecentMessages(Long roomId);
}