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

        System.out.println("RoleBasedFilter - Path: " + request.getServletPath() + ", Role: " + role);

        // Skip if no role header (for WebSocket, public endpoints)
        if (role == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate username from role (or use role as identifier)
        String username = role.toLowerCase() + "-user";

        // Set UserContext with role only
        UserContext.setCurrentUser(username, role);

        // Create Spring Security Authentication with role only
        String springRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(springRole)
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("Spring Security Authentication set for role: " + springRole);

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}