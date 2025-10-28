package com.OfficeManagement.OfficeProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RoleBasedFilter roleBasedFilter;

    public SecurityConfig(RoleBasedFilter roleBasedFilter) {
        this.roleBasedFilter = roleBasedFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**", "/ws-chat/**", "/uploads/**", "/api/files/download/**").permitAll()

                        // Employee management
                        .requestMatchers("/employees/**").hasAnyRole("HR", "DIRECTOR", "CTO")

                        // Department management
                        .requestMatchers("/departments/**").hasAnyRole("HR", "DIRECTOR", "CTO")

                        // Profile
                        .requestMatchers("/profile/**").authenticated()

                        // Task management
                        .requestMatchers("/tasks/**").hasAnyRole("PROJECT_MANAGER", "HR", "DIRECTOR", "CTO")

                        // Chat
                        .requestMatchers("/chat/**").authenticated()

                        // File upload
                        .requestMatchers("/api/files/upload").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(roleBasedFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Role", "X-Username"));
        configuration.setExposedHeaders(Arrays.asList("X-Role", "X-Username"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}