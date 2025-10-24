package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.ChatMessageDTO;
import com.OfficeManagement.OfficeProject.dtos.UserContext;
import com.OfficeManagement.OfficeProject.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable Long roomId) {
        List<ChatMessageDTO> messages = chatService.getRoomMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO) {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();

        if (role == null) {
            role = "USER";
        }

        if (username == null) {
            username = messageDTO.getSenderName();
            if (username == null) {
                username = getDefaultNameFromRole(role);
            }
        }

        if (role.startsWith("ROLE_")) {
            role = role.replace("ROLE_", "");
        }

        chatService.sendMessage(messageDTO, role, username);
    }

    private String getDefaultNameFromRole(String role) {
        switch (role.toUpperCase()) {
            case "DIRECTOR": return "Director";
            case "HR": return "HR Manager";
            case "CTO": return "CTO";
            case "PROJECT_MANAGER": return "Project Manager";
            case "USER": return "Team Member";
            default: return role;
        }
    }
}