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
import org.springframework.security.config.http.SessionCreationPolicy;
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

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;// CustomUserDetailsService
    private final String rememberMeKey;

    @Autowired
    public AppSecurityConfig(
            UserDetailsService userDetailsService,
            @Value("${remember.me.key:default-secret-key}") String rememberMeKey,
            JwtAuthenticationFilter jwtAuthenticationFilter // Constructor Param (property-driven) App.properties
    ) {
        this.userDetailsService = userDetailsService;
        this.rememberMeKey = rememberMeKey;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")  // Endast API

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")  // Alla andra requests (webb)

                .csrf(csrf -> csrf.disable()) // För enkelhet. Du KAN aktivera den om du vill.

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/static/**").permitAll()
                        .requestMatchers("/admin", "/tools").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/authenticate")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/user", true)
                        .failureUrl("/login?error")
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                )

                .rememberMe(rm -> rm
                        .key(rememberMeKey)
                        .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(24))
                        .userDetailsService(userDetailsService)
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