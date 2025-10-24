package com.OfficeManagement.OfficeProject.config;

import com.OfficeManagement.OfficeProject.dtos.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class RoleBasedFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Exclude WebSocket endpoints from this filter
        return path.startsWith("/ws-chat") || path.contains("websocket");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String role = request.getHeader("X-Role");
        String username = request.getHeader("X-Username"); // ADD THIS HEADER

        System.out.println("RoleBasedFilter - Path: " + request.getServletPath() + ", Role: " + role + ", Username: " + username);

        // Skip if no role header (for WebSocket, public endpoints)
        if (role == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // If no username header, generate from role (fallback)
        if (username == null || username.trim().isEmpty()) {
            username = generateUsernameFromRole(role);
            System.out.println("No username header, generated: " + username);
        }

        // Set UserContext with BOTH username and role - THIS IS CRITICAL
        UserContext.setCurrentRole(role);
        UserContext.setCurrentUsername(username); // THIS IS WHAT YOU'RE MISSING

        System.out.println("UserContext set - Username: " + username + ", Role: " + role);

        // Create Spring Security Authentication with role only
        String springRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(springRole)
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("Spring Security Authentication set for user: " + username + " with role: " + springRole);

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    // Helper method to generate proper usernames from roles
    private String generateUsernameFromRole(String role) {
        switch (role.toUpperCase()) {
            case "DIRECTOR": return "company-director";
            case "HR": return "hr-manager";
            case "CTO": return "chief-technology-officer";
            case "PROJECT_MANAGER": return "project-manager";
            case "USER": return "team-member";
            default: return role.toLowerCase() + "-user";
        }
    }
}