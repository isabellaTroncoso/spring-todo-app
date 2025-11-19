package org.example.todoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/* Konfigurerar vilka domäner som får anropa vår backend,
 detta är viktigt för att undvika CORS-fel när frontend och backend ligger på olika
portar. */

@Configuration
public class AppCorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Whitelist
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000", "http://172.0.0.1:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST"));
        corsConfiguration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
        corsConfiguration.setAllowCredentials(true);

        // Backend related endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/register", corsConfiguration);
        source.registerCorsConfiguration("/api/v1/who-am-i", corsConfiguration);
        source.registerCorsConfiguration("/**", corsConfiguration); // ENABLE EVERYTHING

        return source;
    }

}
