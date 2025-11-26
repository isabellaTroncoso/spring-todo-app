package org.example.todoapp.user;

import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final CustomUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(CustomUserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CustomUser registerUser(CustomUser user, String rawPassword) {
        user.setPassword(rawPassword, passwordEncoder);
        return userRepository.save(user);
    }

    public Optional<CustomUser> findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    public Optional<CustomUser> findById(UUID userId) {
        return userRepository.findById(userId);
    }
}
