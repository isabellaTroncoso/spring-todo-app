package org.example.todoapp.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.todoapp.security.jwt.JwtUtils;
import org.example.todoapp.user.custom.CustomUserDetails;
import org.example.todoapp.user.dto.CustomUserLoginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/* Authentication controller: hanterar login
 Skapar JWT-token efter lyckad autentisering
 Sätter token i cookie så frontend kan använda den */

@RestController
public class AuthenticationRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationRestController(JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody CustomUserLoginDTO customUserLoginDTO,
            HttpServletResponse response
    ) {
        logger.debug("Attempting authentication for user: {}", customUserLoginDTO.username());

        // Step 1: Perform authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        customUserLoginDTO.username(),
                        customUserLoginDTO.password())
        );

        // 🧩 DEBUG: Print full Authentication result
        System.out.println("\n========= AUTHENTICATION RESULT =========");
        System.out.println("Class: " + authentication.getClass().getSimpleName());
        System.out.println("Authenticated: " + authentication.isAuthenticated());

        Object principal = authentication.getPrincipal();
        System.out.println("Principal type: " + principal.getClass().getSimpleName());
        if (principal instanceof CustomUserDetails userDetails) {
            System.out.println("  Username: " + userDetails.getUsername());
            System.out.println("  Authorities: " + userDetails.getAuthorities());
            System.out.println("  Account Non Locked: " + userDetails.isAccountNonLocked());
            System.out.println("  Account Enabled: " + userDetails.isEnabled());
            System.out.println("  Password (hashed): " + userDetails.getPassword());
        } else {
            System.out.println("Principal value: " + principal);
        }

        System.out.println("Credentials: " + authentication.getCredentials());
        System.out.println("Details: " + authentication.getDetails());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("=========================================\n");

        // Step 2: Extract your custom principal
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        // Step 3: Generate JWT using your domain model (now includes roles)
        String token = jwtUtils.generateJwtToken(customUserDetails.getCustomUser());

        // DEBUG: visa JWT-token i konsolen
        System.out.println("Generated JWT token: " + token);

        // Step 4: Set cookie
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ✅ change to true in production (HTTPS only)
        cookie.setPath("/");
        cookie.setMaxAge(3600); // 1 hour
        response.addCookie(cookie);

        logger.info("Authentication successful for user: {}", customUserLoginDTO.username());

        // Step 5: Return token - Optional
        return ResponseEntity.ok(Map.of(
                "username", customUserLoginDTO.username(),
                "authorities", customUserDetails.getAuthorities(),
                "token", token
        ));
    }
}