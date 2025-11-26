package org.example.todoapp.config;


import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AdminSetupConfig {

    @Bean
    public CommandLineRunner createAdminUser(CustomUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername("admin")) {
                CustomUser admin = new CustomUser(
                        "admin",
                        encoder.encode("admin123"), // lösenord
                        true, true, true, true,
                        Set.of(UserRole.ADMIN)
                );
                repo.save(admin);
                System.out.println("Admin user created!");
            }
        };
    }
}
