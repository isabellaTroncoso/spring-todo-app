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

    // ================= CREATE TODO =================
    @PostMapping("/user/todos")
    public String createTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute("newTodo") Todo todo) {
        CustomUser user = userDetails.getCustomUser();
        todo.setUser(user);
        todoService.createTodo(todo);
        return "redirect:/user";
    }

    // ================= EDIT & UPDATE TODO =================
    @GetMapping("/user/todos/edit/{id}")
    public String editTodoPage(@PathVariable UUID id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Optional<Todo> optionalTodo = todoService.getTodoById(id);
        if (optionalTodo.isEmpty()) return "redirect:/user";

        Todo todo = optionalTodo.get();
        CustomUser user = userDetails.getCustomUser();

        if (!todo.getUser().getId().equals(user.getId()) && !user.getRoles().contains(UserRole.ADMIN)) {
            return "redirect:/user";
        }

        model.addAttribute("todo", todo);
        return "todo-edit"; // skapar ett Thymeleaf formulär
    }

    @PostMapping("/user/todos/update/{id}")
    public String updateTodo(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute Todo form) {
        todoService.updateTodoIfAllowed(id, form, userDetails.getCustomUser());
        return "redirect:/user";
    }

    // ================= DELETE TODO =================
    @PostMapping("/user/todos/delete/{id}")
    public String deleteTodo(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        todoService.deleteTodoIfAllowed(id, userDetails.getCustomUser());
        return "redirect:/user";
    }

    // ================= TOGGLE COMPLETE =================
    @PostMapping("/user/todos/toggle/{id}")
    public String toggleTodo(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        todoService.toggleTodoCompleted(id, userDetails.getCustomUser());
        return "redirect:/user";
    }

    // Todo - Fix admin roles and authorities
    // ================== ADMIN DASHBOARD ==================
    @GetMapping("/admin")
    public String adminDashboard(@AuthenticationPrincipal CustomUserDetails adminDetails, Model model) {
        // Hämta alla användare
        List<CustomUser> users = customUserRepository.findAll();
        System.out.println("Authorities: " + adminDetails.getAuthorities());

        // Lägg till inloggad admin för att kunna undvika att radera sig själv
        model.addAttribute("currentAdmin", adminDetails.getCustomUser());
        model.addAttribute("users", users);

        return "admin";
    }

    // Ge user ADMIN-roll
    @PostMapping("/admin/make-admin/{id}")
    public String makeAdmin(@PathVariable UUID id,
                            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        Optional<CustomUser> optionalUser = customUserRepository.findById(id);
        optionalUser.ifPresent(user -> {
            // Säkerhetscheck: låt admin inte göra sig själv till admin igen (ej nödvändigt men säkerhet)
            if (!user.getId().equals(adminDetails.getCustomUser().getId())) {
                user.getRoles().add(UserRole.ADMIN);
                customUserRepository.save(user);
            }
        });
        return "redirect:/admin";
    }

    // Radera user (ej sig själv)
    @PostMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable UUID id,
                             @AuthenticationPrincipal CustomUserDetails adminDetails) {
        Optional<CustomUser> optionalUser = customUserRepository.findById(id);
        optionalUser.ifPresent(user -> {
            if (!user.getId().equals(adminDetails.getCustomUser().getId())) {
                customUserRepository.delete(user);
            }
        });
        return "redirect:/admin";
    }
}