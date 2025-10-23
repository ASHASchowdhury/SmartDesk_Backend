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
        // Get role from UserContext (set by RoleBasedFilter)
        String role = UserContext.getCurrentRole();

        if (role == null) {
            role = "USER"; // Default role if not set
            System.out.println("No role found in UserContext, using default: " + role);
        }

        // Remove ROLE_ prefix if present
        if (role.startsWith("ROLE_")) {
            role = role.replace("ROLE_", "");
        }

        System.out.println("Sending chat message with role: " + role);

        // Send message with role from RoleBasedFilter
        chatService.sendMessage(messageDTO, role);
    }
}