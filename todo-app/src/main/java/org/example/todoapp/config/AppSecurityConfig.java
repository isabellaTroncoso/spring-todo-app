package org.example.todoapp.config;
import jakarta.servlet.http.HttpServletResponse;
import org.example.todoapp.security.jwt.JwtAuthenticationFilter;
import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.concurrent.TimeUnit;

/* Security bestämmer vilka URL:er som kräver autentisering
 och vilka som är offentliga, samt lägger till JWT-filter
 */

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {

    private final UserDetailsService userDetailsService;    // CustomUserDetailsService
    private final String rememberMeKey;

    @Autowired
    public AppSecurityConfig(
            UserDetailsService userDetailsService,
            @Value("{remember.me.key}") String rememberMeKey    // Constructor Param (property-driven) App.properties
    ) {
        this.userDetailsService = userDetailsService;
        this.rememberMeKey = rememberMeKey;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrfConfigurer -> csrfConfigurer.disable())   // Disable for DEBUGGING PURPOSES
                .authorizeHttpRequests( auth -> auth
                        // .requestMatchers() // TODO - check against specific HTTP METHOD
                        .requestMatchers("/", "/login", "/register", "/static/**").permitAll()  // Allow localhost:8080/
                        .requestMatchers("/debug/**").permitAll()                     // RestController for Debugging
                        .requestMatchers("/admin", "/tools").hasRole("ADMIN")
                        .requestMatchers("/user").hasRole(UserRole.USER.name())
                        .anyRequest().authenticated() // MUST exist AFTER matchers
                )

                // TODO - Logging for Authentication
                .formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer
                                .loginPage("/login").permitAll()
                                .loginProcessingUrl("/authenticate") // default is /login for processing auth
                                .usernameParameter("username")  // Must match HTML param
                                .passwordParameter("password")
                                .failureUrl("/login?error")
                                .defaultSuccessUrl("/", false)      // Redirect after login->home (false by default)
                        // .failureForwardUrl("")                                   // Handle Login Attempts
                        // .successForwardUrl("")                                   // Handles Success Logic
                )

                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutUrl("/logout").permitAll()
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .logoutSuccessUrl("/login?logout")          // Redirect -> Login
                )

                .rememberMe(rememberMeConfigurer -> rememberMeConfigurer
                        .key(rememberMeKey)            // Some SECURE key
                        .rememberMeParameter("remember-me")           // remember-me default
                        .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(24)) // 24 days
                        .userDetailsService(userDetailsService) // Use Our CustomUser Implementation
                )

                .sessionManagement(session -> session
                        // How long an inactive session lasts
                        .invalidSessionUrl("/login?invalid")
                        .maximumSessions(1)                 // 🔒 Max 1 concurrent session per user
                        .maxSessionsPreventsLogin(false)     // false = old session invalidated
                        .expiredUrl("/login?expired")        // redirect if user logs in elsewhere
                );


        return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

}