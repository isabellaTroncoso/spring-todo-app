package org.example.todoapp.config;
import org.example.todoapp.user.authority.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.concurrent.TimeUnit;

/* Security bestämmer vilka URL:er som kräver autentisering
 och vilka som är offentliga, samt lägger till JWT-filter
 */

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {

    private final UserDetailsService userDetailsService;
    private final String rememberMeKey;

    @Autowired
    public AppSecurityConfig(
            UserDetailsService userDetailsService,
            @Value("{remember.me.key}") String rememberMeKey
    ) {
        this.userDetailsService = userDetailsService;
        this.rememberMeKey = rememberMeKey;
    }

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager (för min login-controller)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {


        httpSecurity
                .csrf(csrfConfigurer -> csrfConfigurer.disable()) // Disable for DEBUGGING PURPOSES
                .authorizeHttpRequests( auth -> auth
                        // .requestMatchers()
                        .requestMatchers("/", "/register", "/static/**").permitAll()  // Allow localhost:8080/
                        .requestMatchers("/debug/**").permitAll()                     // RestController for Debugging
                        .requestMatchers("/admin", "/tools").hasRole("ADMIN")
                        .requestMatchers("/user").hasRole(UserRole.USER.name())
                        .anyRequest().authenticated()
                )

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
                        .key(rememberMeKey)
                        .rememberMeParameter("remember-me")           // remember-me default
                        .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(24)) // 24 days
                        .userDetailsService(userDetailsService) // Use Our CustomUser Implementation
                )

                .sessionManagement(session -> session
                        // How long an inactive session lasts
                        // .invalidSessionUrl("/login?invalid")                     // Prevent Login Problems
                        .maximumSessions(1)                         // Max 1 concurrent session per user
                        .maxSessionsPreventsLogin(false)     // false = old session invalidated
                        .expiredUrl("/login?expired")                    // redirect if user logs in elsewhere
                );


        return httpSecurity.build();
    }

}