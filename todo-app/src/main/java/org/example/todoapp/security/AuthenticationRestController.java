package org.example.todoapp.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
//import org.example.todoapp.config.RabbitConfig;
import jakarta.validation.Valid;
import org.example.todoapp.config.RabbitConfig;
import org.example.todoapp.security.jwt.JwtUtils;
import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserDetails;
import org.example.todoapp.user.dto.CustomUserCreationDTO;
import org.example.todoapp.user.dto.CustomUserLoginDTO;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/* Authentication controller: hanterar login
 Skapar JWT-token efter lyckad autentisering
 Sätter token i cookie så frontend kan använda den */

@RestController
@RequestMapping("/api/auth")
public class AuthenticationRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AmqpTemplate amqpTemplate;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public AuthenticationRestController(JwtUtils jwtUtils, AuthenticationManager authenticationManager,
                                        CustomUserRepository customUserRepository, PasswordEncoder passwordEncoder,
                                        AmqpTemplate amqpTemplate, RabbitTemplate rabbitTemplate) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.customUserRepository = customUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.amqpTemplate = amqpTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid
                                                  @RequestBody CustomUserLoginDTO customUserLoginDTO,
            HttpServletResponse response
    ) {
        logger.info("DEBUG: /api/auth/login called");
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
            //System.out.println("  Password (hashed): " + userDetails.getPassword());
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
        System.out.println(customUserLoginDTO);

        // Step 4: Set cookie
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ✅ change to true in production (HTTPS only)
        cookie.setPath("/");
        cookie.setMaxAge(3600); // 1 hour
        response.addCookie(cookie);

        logger.info("Authentication successful for user: {}", customUserLoginDTO.username());

        // RabbitMQ
        amqpTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                "User Logged in, todo: send email to user to alert them of login from weird IP addresses"
        );

        // Step 5: Return token - Optional
        return ResponseEntity.ok(Map.of(
                "username", customUserLoginDTO.username(),
                "authorities", customUserDetails.getAuthorities(),
                "token", token
        ));


    }

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CustomUserCreationDTO dto) {

        if (customUserRepository.existsByUsername(dto.username())) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Username already exists"
            ));
        }

        CustomUser user = new CustomUser(
                dto.username(),
                passwordEncoder.encode(dto.password()),
                true, true, true, true,
                Set.of(UserRole.USER) // standard role USER
        );

        customUserRepository.save(user);

        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "User registered successfully",
                "username", user.getUsername()
        ));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        CustomUser user = userDetails.getCustomUser();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Enum::name).toList()
        ));
    }


    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true i produktion
        cookie.setPath("/");
        cookie.setMaxAge(0); // radera cookie
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }
}
