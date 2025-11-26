package org.example.todoapp.view;

import jakarta.validation.Valid;
import org.example.todoapp.todos.Todo;
import org.example.todoapp.todos.TodoService;
import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserDetails;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.example.todoapp.user.dto.CustomUserCreationDTO;
import org.example.todoapp.user.mapper.CustomUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Controller
public class CustomViewController {

    // TODO - Replace with Service in the future
    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserMapper customUserMapper;
    private final TodoService todoService;

    @Autowired
    public CustomViewController(CustomUserRepository customUserRepository,
                                PasswordEncoder passwordEncoder,
                                CustomUserMapper customUserMapper, TodoService todoService) {
        this.customUserRepository = customUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserMapper = customUserMapper;
        this.todoService = todoService;
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

    )
    {
        System.out.println(customUserCreationDTO);
        System.out.println("step one");
        if (bindingResult.hasErrors()) {
            System.out.println("Binding results");
            return "registerpage";
        }

        if (customUserRepository.existsByUsername(customUserCreationDTO.username())) {
            bindingResult.rejectValue("username", "error.username", "Username already exists");
            System.out.println("Reject value");
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
        System.out.println("DTO username = '" + customUserCreationDTO.username() + "'");
        System.out.println("DTO password = '" + customUserCreationDTO.password() + "'");

        return "redirect:/login";
    }

    @GetMapping("/user")
    public String userTodosPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        CustomUser user = userDetails.getCustomUser(); // extrahera entity
        List<Todo> todos = todoService.getTodosForUser(user);
        model.addAttribute("todos", todos);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("newTodo", new Todo());
        return "user";
    }

    // Skapa ny todo
    @PostMapping("/user/todos")
    public String createTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute("newTodo") Todo todo) {
        CustomUser user = userDetails.getCustomUser();
        todo.setUser(user);
        todoService.createTodo(todo);
        return "redirect:/user";
    }

    // Uppdatera todo
    @PostMapping("/user/todos/edit/{id}")
    public String updateTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable UUID id,
                             @ModelAttribute Todo todoForm) {
        CustomUser user = userDetails.getCustomUser();
        Todo todo = todoService.getTodoById(id).orElse(null);
        if (todo != null && (todo.getUser().getId().equals(user.getId()) || user.getRoles().contains(UserRole.ADMIN))) {
            todo.setTitle(todoForm.getTitle());
            todo.setDescription(todoForm.getDescription());
            todo.setCompleted(todoForm.isCompleted());
            todoService.updateTodo(todo);
        }
        return "redirect:/user";
    }

    // Radera todo
    @PostMapping("/user/todos/delete/{id}")
    public String deleteTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable UUID id) {
        CustomUser user = userDetails.getCustomUser();
        Todo todo = todoService.getTodoById(id).orElse(null);
        if (todo != null && (todo.getUser().getId().equals(user.getId()) || user.getRoles().contains(UserRole.ADMIN))) {
            todoService.deleteTodo(id);
        }
        return "redirect:/user";
    }

    @PostMapping("/user/{id}/make-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> makeAdmin(@PathVariable UUID id) {
        Optional<CustomUser> optionalUser = customUserRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        CustomUser user = optionalUser.get();
        user.getRoles().add(UserRole.ADMIN);
        customUserRepository.save(user);
        return ResponseEntity.ok("User is now admin");
    }

}