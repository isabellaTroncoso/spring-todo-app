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

    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserMapper customUserMapper;
    private final TodoService todoService;

    @Autowired
    public CustomViewController(CustomUserRepository customUserRepository,
                                PasswordEncoder passwordEncoder,
                                CustomUserMapper customUserMapper,
                                TodoService todoService) {
        this.customUserRepository = customUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserMapper = customUserMapper;
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String root() {
        return "login";
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

        String hashedPassword = passwordEncoder.encode(customUserCreationDTO.password());

        CustomUser customUser = new CustomUser(
                customUserCreationDTO.username(),
                hashedPassword,
                true,
                true,
                true,
                true,
                Set.of(UserRole.USER)
        );

        customUserRepository.save(customUser);

        return "redirect:/login";
    }

    // ================= USER TODOS =================
    @GetMapping("/user")
    public String userTodosPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        CustomUser user = userDetails.getCustomUser();

        // ================= USER TODOS =================
        List<Todo> todos = todoService.getTodosForUser(user);
        model.addAttribute("todos", todos);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("newTodo", new Todo());

        // ================= ADMIN PANEL =================
        if (user.isAdmin()) {
            List<CustomUser> users = customUserRepository.findAll();
            model.addAttribute("users", users);
            model.addAttribute("currentAdmin", user);
        }

        return "user";
    }


    @PostMapping("/user/todos")
    public String createTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute("newTodo") Todo todo) {
        CustomUser user = userDetails.getCustomUser();
        todo.setUser(user);
        todoService.createTodo(todo);
        return "redirect:/user";
    }

    @GetMapping("/user/todos/edit/{id}")
    public String editTodoPage(@PathVariable UUID id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        CustomUser user = userDetails.getCustomUser();
        Optional<Todo> optionalTodo = getTodoIfAllowed(id, user);
        if (optionalTodo.isEmpty()) return "redirect:/user";

        model.addAttribute("todo", optionalTodo.get());
        return "todo-edit";
    }

    @PostMapping("/user/todos/update/{id}")
    public String updateTodo(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute Todo form) {
        todoService.updateTodoIfAllowed(id, form, userDetails.getCustomUser());
        return "redirect:/user";
    }

    @PostMapping("/user/todos/delete/{id}")
    public String deleteTodo(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        CustomUser user = userDetails.getCustomUser();
        Optional<Todo> optionalTodo = getTodoIfAllowed(id, user);
        optionalTodo.ifPresent(todo -> todoService.deleteTodoIfAllowed(id, user));
        return "redirect:/user";
    }

    @PostMapping("/user/todos/toggle/{id}")
    public String toggleTodo(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        CustomUser user = userDetails.getCustomUser();
        Optional<Todo> optionalTodo = getTodoIfAllowed(id, user);
        optionalTodo.ifPresent(todo -> todoService.toggleTodoCompleted(id, user));
        return "redirect:/user";
    }

    // ================= ADMIN DASHBOARD =================
    @GetMapping("/admin")
    public String adminRedirect(@AuthenticationPrincipal CustomUserDetails adminDetails) {

        return "redirect:/user";
    }

    @PostMapping("/admin/make-admin/{id}")
    public String makeAdmin(@PathVariable UUID id,
                            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        Optional<CustomUser> optionalUser = customUserRepository.findById(id);
        optionalUser.ifPresent(user -> {
            if (!user.getId().equals(adminDetails.getCustomUser().getId())) {
                user.getRoles().add(UserRole.ADMIN);
                customUserRepository.save(user);
            }
        });
        return "redirect:/user";
    }

    @PostMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails adminDetails) {
        Optional<CustomUser> optionalUser = customUserRepository.findById(id);
        optionalUser.ifPresent(user -> {
            if (!user.getId().equals(adminDetails.getCustomUser().getId())) {
                customUserRepository.delete(user);
            }
        });
        return "redirect:/user";
    }

    // ================= PRIVATE HELPERS =================
    private Optional<Todo> getTodoIfAllowed(UUID id, CustomUser user) {
        Optional<Todo> optionalTodo = todoService.getTodoById(id);
        if (optionalTodo.isEmpty()) return Optional.empty();

        Todo todo = optionalTodo.get();
        if (!todo.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            return Optional.empty();
        }
        return Optional.of(todo);
    }
}