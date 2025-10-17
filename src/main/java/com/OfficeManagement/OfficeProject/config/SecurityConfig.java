package com.OfficeManagement.OfficeProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS - No authentication required
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ai/**").permitAll()  // Allow AI without authentication
                        .requestMatchers("/error").permitAll()

                        // PROTECTED ENDPOINTS - Role-based access
                        .requestMatchers("/departments/**").hasAnyRole("HR", "DIRECTOR", "CTO")
                        .requestMatchers("/employees/**").hasAnyRole("HR", "DIRECTOR", "CTO", "PROJECT_MANAGER")
                        .requestMatchers("/tasks/**").hasAnyRole("PROJECT_MANAGER", "CTO", "DIRECTOR")
                        .requestMatchers("/profile/**").authenticated()

                        // Any other request needs authentication
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
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}