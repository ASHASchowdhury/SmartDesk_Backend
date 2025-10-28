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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class RoleBasedFilter extends OncePerRequestFilter {

    private static final List<String> VALID_ROLES = Arrays.asList(
            "DIRECTOR", "HR", "CTO", "PROJECT_MANAGER", "USER"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        return path.startsWith("/ws-chat") ||
                path.contains("websocket") ||
                path.startsWith("/auth") ||
                path.startsWith("/uploads/") ||
                method.equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String role = request.getHeader("X-Role");
        String username = request.getHeader("X-Username");

        System.out.println("RoleBasedFilter - Path: " + request.getServletPath() +
                ", Method: " + request.getMethod() +
                ", Role: " + role + ", Username: " + username);

        // For public endpoints, allow without authentication
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate headers for protected endpoints
        if (role == null || username == null || username.trim().isEmpty()) {
            System.out.println("Missing authentication headers for protected endpoint");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication headers");
            return;
        }

        // Validate role
        String normalizedRole = role.toUpperCase();
        if (!VALID_ROLES.contains(normalizedRole)) {
            System.out.println("Invalid role: " + role);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Invalid user role");
            return;
        }

        try {
            // Set UserContext
            UserContext.setCurrentRole(normalizedRole);
            UserContext.setCurrentUsername(username);

            System.out.println("UserContext set - Username: " + username + ", Role: " + normalizedRole);

            // Create Spring Security Authentication
            String springRole = normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole;

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(springRole)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Spring Security Authentication set for user: " + username + " with role: " + springRole);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/api/files/download/");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\", \"status\": " + status + "}");
    }
}