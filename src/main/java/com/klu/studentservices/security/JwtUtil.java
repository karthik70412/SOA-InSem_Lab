package com.klu.studentservices.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String SECRET_KEY =
            "MyVerySecretKeyForStudentMicroserviceJwtAuthentication2026";

    private static final long EXPIRATION_TIME =
            1000 * 60 * 30; // 30 minutes

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Generate token with username only (default USER role)
     */
    public String generateToken(String username) {
        return generateToken(username, Collections.singletonList("USER"));
    }

    /**
     * Generate token with username and roles
     */
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate an expired token for testing
     */
    public String generateExpiredToken(String username, List<String> roles) {
        Date now = new Date();
        Date pastIssue = new Date(now.getTime() - (1000 * 60 * 60)); // 1 hour ago
        Date pastExpiry = new Date(now.getTime() - (1000 * 60 * 10)); // expired 10 minutes ago

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(pastIssue)
                .expiration(pastExpiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parse and decode the JWT token using the secret key.
     * Verifies token signature, checks expiration, and returns Claims.
     * Throws ExpiredJwtException if expired.
     * Throws MalformedJwtException / JwtException / IllegalArgumentException if invalid.
     */
    public Claims parseAndValidateToken(String token) throws ExpiredJwtException, JwtException, IllegalArgumentException {
        if (token == null || token.trim().isEmpty()) {
            throw new MalformedJwtException("Token cannot be null or empty");
        }
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }

    /**
     * Extract username from token claims
     */
    public String extractUsername(String token) {
        return parseAndValidateToken(token).getSubject();
    }

    /**
     * Extract roles from Claims
     */
    public List<String> extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj == null) {
            rolesObj = claims.get("role");
        }
        if (rolesObj instanceof List<?>) {
            List<?> rawList = (List<?>) rolesObj;
            List<String> roles = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj != null) {
                    roles.add(obj.toString());
                }
            }
            return roles;
        } else if (rolesObj instanceof String) {
            String roleStr = (String) rolesObj;
            if (roleStr.startsWith("[") && roleStr.endsWith("]")) {
                roleStr = roleStr.substring(1, roleStr.length() - 1);
                String[] parts = roleStr.split(",");
                List<String> roles = new ArrayList<>();
                for (String part : parts) {
                    String clean = part.trim().replaceAll("^\"|\"$", "");
                    if (!clean.isEmpty()) {
                        roles.add(clean);
                    }
                }
                return roles;
            }
            return Collections.singletonList(roleStr);
        }
        return Collections.emptyList();
    }

    /**
     * Extract roles directly from token string
     */
    public List<String> extractRoles(String token) {
        Claims claims = parseAndValidateToken(token);
        return extractRoles(claims);
    }

    /**
     * Check whether token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseAndValidateToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract specific claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAndValidateToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Internal claims getter for backward compatibility
     */
    private Claims getClaims(String token) {
        return parseAndValidateToken(token);
    }
}