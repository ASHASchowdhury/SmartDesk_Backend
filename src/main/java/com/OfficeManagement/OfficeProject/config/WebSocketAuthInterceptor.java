package com.OfficeManagement.OfficeProject.config;

import com.OfficeManagement.OfficeProject.dtos.UserContext;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        System.out.println("🔌 WebSocket handshake initiated");

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // Get role and username from headers or parameters
            String role = httpRequest.getHeader("X-Role");
            String username = httpRequest.getHeader("X-Username");

            // Also check query parameters as fallback
            if (role == null) {
                role = httpRequest.getParameter("role");
            }
            if (username == null) {
                username = httpRequest.getParameter("username");
            }

            System.out.println("WebSocket Auth - Role: " + role + ", Username: " + username);

            if (role != null) {
                // Set UserContext for WebSocket connection
                UserContext.setCurrentRole(role);
                UserContext.setCurrentUsername(username != null ? username : generateUsernameFromRole(role));

                // Store in attributes for later use
                attributes.put("userRole", role);
                attributes.put("username", username != null ? username : generateUsernameFromRole(role));

                System.out.println("✅ WebSocket authenticated - Username: " +
                        UserContext.getCurrentUsername() + ", Role: " + UserContext.getCurrentRole());
            } else {
                System.out.println("⚠️ No role provided in WebSocket handshake");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Clean up UserContext after handshake
        UserContext.clear();
        System.out.println("🔌 WebSocket handshake completed");
    }

    private String generateUsernameFromRole(String role) {
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