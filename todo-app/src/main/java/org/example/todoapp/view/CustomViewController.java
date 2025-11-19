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
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Set;

@Controller
public class CustomViewController {


    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserMapper customUserMapper;

    @Autowired
    public CustomViewController(CustomUserRepository customUserRepository, PasswordEncoder passwordEncoder, CustomUserMapper customUserMapper) {
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

    @GetMapping("/admin")
    public String adminPage() {

        return "adminpage"; // Must Reflect .html document name
    }

    @GetMapping("/user")
    public String userPage() {

        return "userpage";
    }

    // Responsible for Inserting CustomUser Entity (otherwise DTO)
    @GetMapping("/register")
    public String registerPage(Model model) {

        // Best practice: id aka AttributeName should be the same as object name
        model.addAttribute("customUser", new CustomUser());

        return "registerpage";
    }

    // Handles Business Logic - coming from SUBMIT FORM
    @PostMapping("/register")
    public String registerUser(
            @Valid CustomUserCreationDTO customUserCreationDTO, BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            return "registerpage";
        }

        CustomUser customUser = customUserMapper.toEntity(customUserCreationDTO);

        customUser.setPassword(
                customUser.getPassword(),
                passwordEncoder
        );


        customUser.setAccountNonExpired(true);
        customUser.setAccountNonLocked(true);
        customUser.setCredentialsNonExpired(true);
        customUser.setEnabled(true);


        customUser.setUserRoles(
                Set.of(UserRole.USER)
        );

        System.out.println("Saving user... ");
        customUserRepository.save(customUser);

        return "redirect:/login";
    }

}