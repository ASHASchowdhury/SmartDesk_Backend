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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String username = request.getHeader("X-Username");
        String role = request.getHeader("X-Role");

        System.out.println("RoleBasedFilter - Path: " + request.getServletPath() +
                ", Username: " + username + ", Role: " + role);

        // Handle missing headers - provide sensible defaults
        if (username == null || role == null) {
            username = "default-user";
            role = "USER"; // Default role
        }

        // Set UserContext
        UserContext.setCurrentUser(username, role);

        // CREATE SPRING SECURITY AUTHENTICATION
        if (username != null && role != null) {
            // Ensure role has ROLE_ prefix for Spring Security
            String springRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(springRole)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Spring Security Authentication set for: " +
                    username + " with role: " + springRole);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}