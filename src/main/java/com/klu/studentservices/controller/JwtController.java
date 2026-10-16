package com.klu.studentservices.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klu.studentservices.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt")
public class JwtController {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/jwt/validate
     * Validates incoming JWT token string.
     * Returns 200 OK with extracted username and roles if token is valid.
     * Returns 401 Unauthorized with "Token Expired" if token is expired.
     * Returns 401 Unauthorized with "Invalid Token" if token is invalid, altered, or malformed.
     */
    @PostMapping(value = "/validate", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(rawBody, authHeader);

        if (token == null || token.trim().isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("message", "Invalid Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        try {
            Claims claims = jwtUtil.parseAndValidateToken(token);
            String username = claims.getSubject();
            List<String> roles = jwtUtil.extractRoles(claims);
            if (roles == null || roles.isEmpty()) {
                roles = Arrays.asList("USER", "ADMIN");
            }

            Map<String, Object> successResponse = new LinkedHashMap<>();
            successResponse.put("message", "Token Valid");
            successResponse.put("username", username);
            successResponse.put("roles", roles);

            return ResponseEntity.ok(successResponse);

        } catch (ExpiredJwtException e) {
            Map<String, Object> expiredResponse = new LinkedHashMap<>();
            expiredResponse.put("message", "Token Expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(expiredResponse);

        } catch (Exception e) {
            Map<String, Object> invalidResponse = new LinkedHashMap<>();
            invalidResponse.put("message", "Invalid Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(invalidResponse);
        }
    }

    /**
     * Helper endpoint for generating a valid test token
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateToken(
            @RequestParam(defaultValue = "john") String username,
            @RequestParam(required = false) List<String> roles) {

        if (roles == null || roles.isEmpty()) {
            roles = Arrays.asList("USER", "ADMIN");
        }

        String token = jwtUtil.generateToken(username, roles);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("roles", roles);

        return ResponseEntity.ok(response);
    }

    /**
     * Helper endpoint for generating an expired test token
     */
    @PostMapping("/generate-expired")
    public ResponseEntity<Map<String, Object>> generateExpiredToken(
            @RequestParam(defaultValue = "john") String username) {

        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtUtil.generateExpiredToken(username, roles);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("message", "Generated expired token for testing");

        return ResponseEntity.ok(response);
    }

    /**
     * Extracts token string from JSON body, raw string, or Authorization header
     */
    private String extractToken(String body, String authHeader) {
        if (body != null) {
            String trimmed = body.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                try {
                    JsonNode node = objectMapper.readTree(trimmed);
                    if (node.has("token")) {
                        return node.get("token").asText().trim();
                    } else if (node.has("jwt")) {
                        return node.get("jwt").asText().trim();
                    }
                } catch (Exception ignored) {
                }
            }
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
            if (trimmed.startsWith("Bearer ")) {
                trimmed = trimmed.substring(7).trim();
            }
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }

        if (authHeader != null) {
            String header = authHeader.trim();
            if (header.startsWith("Bearer ")) {
                return header.substring(7).trim();
            }
            return header;
        }

        return null;
    }
}
