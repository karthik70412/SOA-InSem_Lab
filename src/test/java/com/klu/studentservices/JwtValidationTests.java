package com.klu.studentservices;

import com.klu.studentservices.controller.JwtController;
import com.klu.studentservices.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class JwtValidationTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtController jwtController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jwtController).build();
    }

    @Test
    @DisplayName("Test JwtUtil directly: generate, parse, validate, and extract claims")
    void testJwtUtilDirect() {
        String username = "john";
        List<String> roles = Arrays.asList("USER", "ADMIN");

        String token = jwtUtil.generateToken(username, roles);
        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token));

        Claims claims = jwtUtil.parseAndValidateToken(token);
        assertEquals("john", claims.getSubject());

        List<String> extractedRoles = jwtUtil.extractRoles(claims);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("USER"));
        assertTrue(extractedRoles.contains("ADMIN"));
    }

    @Test
    @DisplayName("Test Case 1: Pass a valid JWT token – should return 200 Token Valid with username and roles")
    void testValidJwtToken() throws Exception {
        String username = "john";
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String validToken = jwtUtil.generateToken(username, roles);

        // Test with JSON body: {"token": "..."}
        String jsonPayload = "{\"token\": \"" + validToken + "\"}";

        mockMvc.perform(post("/api/jwt/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Token Valid")))
                .andExpect(jsonPath("$.username", is("john")))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", contains("USER", "ADMIN")));

        // Test also with raw token string in body
        mockMvc.perform(post("/api/jwt/validate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Token Valid")))
                .andExpect(jsonPath("$.username", is("john")))
                .andExpect(jsonPath("$.roles", contains("USER", "ADMIN")));
    }

    @Test
    @DisplayName("Test Case 2: Pass an altered JWT token – should return 401 Invalid Token")
    void testAlteredJwtToken() throws Exception {
        String validToken = jwtUtil.generateToken("john", Arrays.asList("USER", "ADMIN"));
        // Alter the token signature by tampering with the last characters
        String alteredToken = validToken.substring(0, validToken.length() - 5) + "xyz99";

        String jsonPayload = "{\"token\": \"" + alteredToken + "\"}";

        mockMvc.perform(post("/api/jwt/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid Token")));
    }

    @Test
    @DisplayName("Test Case 3: Pass an expired JWT token – should return 401 Token Expired")
    void testExpiredJwtToken() throws Exception {
        String expiredToken = jwtUtil.generateExpiredToken("john", Arrays.asList("USER", "ADMIN"));

        String jsonPayload = "{\"token\": \"" + expiredToken + "\"}";

        mockMvc.perform(post("/api/jwt/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Token Expired")));
    }

    @Test
    @DisplayName("Test Case 4: Pass a malformed JWT token – should return 401 Invalid Token")
    void testMalformedJwtToken() throws Exception {
        String malformedToken = "invalid.token.format.here";

        String jsonPayload = "{\"token\": \"" + malformedToken + "\"}";

        mockMvc.perform(post("/api/jwt/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid Token")));
    }
}
