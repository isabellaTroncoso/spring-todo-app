package org.example.todoapp.user.controller;

import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final CustomUserRepository userRepository;

    @Autowired
    public AdminUserController(CustomUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 🔹 List all users (admin only)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomUser> getAllUsers() {
        return userRepository.findAll();
    }

    // 🔹 Delete user by ID (admin only)
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        Optional<CustomUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userRepository.delete(user.get());
        return ResponseEntity.ok("User deleted successfully");
    }
}
