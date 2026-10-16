package com.klu.studentservices.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF for REST API
            .csrf(csrf -> csrf.disable())

            // JWT authentication is stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // URL authorization
            .authorizeHttpRequests(auth -> auth

                // Authentication APIs - no token required
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/jwt/**",
                    "/api/orders",
                    "/api/orders/**"
                ).permitAll()

                // Profile - currently no token required
                .requestMatchers(
                    "/profile",
                    "/profile/**"
                ).permitAll()

                // Everything else requires JWT
                .anyRequest().authenticated()
            )

            // Run JWT filter before Spring's username/password filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}