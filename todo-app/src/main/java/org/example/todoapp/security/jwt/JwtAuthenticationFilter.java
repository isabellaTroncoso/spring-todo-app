package org.example.todoapp.security.jwt;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.todoapp.user.custom.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/* JWT-filter: kontrollerar token på varje request
 Om token är giltig, autentiseras användaren i systemet
*/

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService; // TODO - Change to UserDetailsService

    @Autowired
    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   CustomUserDetailsService customUserDetailsService) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain        // Decides when a filterChain stops
    ) throws ServletException, IOException {

        logger.debug("---- JwtAuthenticationFilter START ----");

        // Extract token
        String token = jwtUtils.extractJwtFromCookie(request);
        if (token == null) {
            token = jwtUtils.extractJwtFromRequest(request); // fallback to Authorization header
        }

        if (token == null) {
            logger.debug("No JWT token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("JWT token found: {}", token);

        // Validate token
        if (jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUsernameFromJwtToken(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Live DB lookup (ensures user still exists / is enabled)
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username); // ENTITY

                // Possibility to check for other userDetails booleans
                if (userDetails != null && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Update Spring with possible new change
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Authenticated (DB verified) user '{}'", username);
                } else {
                    logger.warn("User '{}' not found or disabled", username);
                }
            }
        } else {
            logger.warn("Invalid JWT token");
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
        logger.debug("---- JwtAuthenticationFilter END ----");
    }

}