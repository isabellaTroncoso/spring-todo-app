package org.example.todoapp.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*  JWT-utils: skapar och validerar JWT-token
 Hjälper till att hämta användarinformation från token */

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String base64EncodedSecretKey = "U2VjdXJlQXBpX1NlY3JldEtleV9mb3JfSFMyNTYwX3NlY3JldF9wcm9qZWN0X2tleV9leGFtcGxl";
    private final byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
    private final SecretKey key = Keys.hmacShaKeyFor(keyBytes); // HMAC algorithm

    // JWT expiration time (1 hour)
    private final int jwtExpirationMs = (int) TimeUnit.HOURS.toMillis(1);

    public String generateJwtToken(CustomUser customUser) {
        logger.debug("Generating JWT for user: {} with roles: {}", customUser.getUsername(), customUser.getRoles());

        List<String> roles = customUser.getRoles().stream().map(
                userRole -> userRole.getRoleName()
        ).toList();

        String token = Jwts.builder()
                .subject(customUser.getUsername())  // sub
                .claim("authorities", roles)  // claim: authorities (claim is used for custom fields)
                .issuedAt(new Date())               // iat
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // exp
                .signWith(key)
                .compact();

        logger.info("JWT generated successfully for user: {}", customUser.getUsername());
        return token;
    }

    public String getUsernameFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject(); // Subject, whom the token refers to, principal: whose currently authenticated (system)
            logger.debug("Extracted username '{}' from JWT token", username);
            return username;

        } catch (Exception e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public Set<UserRole> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> authoritiesClaim = claims.get("authorities", List.class);

        if (authoritiesClaim == null || authoritiesClaim.isEmpty()) {
            logger.warn("No authorities found in JWT token");
            return Set.of();
        }

        // Convert each string like "ROLE_USER" -> UserRole.USER
        Set<UserRole> roles = authoritiesClaim.stream()
                .filter(String.class::isInstance) // keep only strings
                .map(String.class::cast)
                .map(role -> role.replace("ROLE_", "")) // remove prefix if necessary
                .map(String::toUpperCase)
                .map(UserRole::valueOf) // map to your enum
                .collect(Collectors.toSet());

        logger.debug("Extracted roles from JWT token: {}", roles);
        return roles;
    }

    // Used to pass in JWT token for Validation
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);

            logger.debug("JWT validation succeeded");
            return true;

        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
        }

        return false;
    }

    // Helper: Extract JWT from cookie
    String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("authToken".equals(cookie.getName())) {     // Cookie should be named authToken
                return cookie.getValue();
            }
        }
        return null;
    }

    // Helper: Extract JWT from Authorization header
    String extractJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}