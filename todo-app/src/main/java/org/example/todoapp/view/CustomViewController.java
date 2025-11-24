package org.example.todoapp.view;

import jakarta.validation.Valid;
import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.example.todoapp.user.dto.CustomUserCreationDTO;
import org.example.todoapp.user.mapper.CustomUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Set;

@Controller
public class CustomViewController {

    // TODO - Replace with Service in the future
    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserMapper customUserMapper;

    @Autowired
    public CustomViewController(CustomUserRepository customUserRepository,
                                PasswordEncoder passwordEncoder, CustomUserMapper customUserMapper) {
        this.customUserRepository = customUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserMapper = customUserMapper;
    }

    @GetMapping("/login")
    public String loginPage() {

        return "login";
    }

    @GetMapping("/logout")
    public String logoutPage() {

        return "logout";
    }

    // ================= REGISTER =================
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("customUser", CustomUserCreationDTO.empty());
        return "registerpage";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("customUser") CustomUserCreationDTO customUserCreationDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "registerpage";
        }

        if (customUserRepository.existsByUsername(customUserCreationDTO.username())) {
            bindingResult.rejectValue("username", "error.username", "Username already exists");
            return "registerpage";
        }

        // Hash DTO-lösenordet först
        String hashedPassword = passwordEncoder.encode(customUserCreationDTO.password());

        // Skapa entity med hashat lösenord
        CustomUser customUser = new CustomUser(
                customUserCreationDTO.username(),
                hashedPassword,
                true,
                true,
                true,
                true,
                Set.of(UserRole.USER)
        );

        // Spara användare
        customUserRepository.save(customUser);

        return "redirect:/login";
    }
}