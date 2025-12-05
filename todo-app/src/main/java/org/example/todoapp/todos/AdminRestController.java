package org.example.todoapp.todos;

import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.example.todoapp.user.authority.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final CustomUserRepository userRepository;

    public AdminRestController(CustomUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ================= GET ALL USERS =================
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ================= MAKE ADMIN =================
    @PostMapping("/make-admin/{id}")
    public ResponseEntity<?> makeAdmin(@PathVariable UUID id) {
        CustomUser user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Lägg till enum-rollen, inte en string
        user.getRoles().add(UserRole.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User promoted to admin"
        ));
    }

    // ================= DELETE USER =================
    // ================= DELETE USER =================
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id, Authentication auth) {
        CustomUser target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        // Hämta inloggad admin
        String loggedInUsername = auth.getName();
        CustomUser loggedInUser = userRepository.findUserByUsername(loggedInUsername)
                .orElseThrow(() -> new RuntimeException("Logged-in admin not found"));

        // Admin kan inte radera sig själv
        if (loggedInUser.getId().equals(id)) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Admins cannot delete themselves"
            ));
        }

        // Ta bort användaren
        userRepository.deleteById(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
        ));
    }
}