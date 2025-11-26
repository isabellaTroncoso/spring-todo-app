package org.example.todoapp.todos;

import jakarta.validation.Valid;
import org.example.todoapp.todos.dto.TodoCreateDTO;
import org.example.todoapp.todos.dto.TodoResponseDTO;
import org.example.todoapp.todos.dto.TodoUpdateDTO;
import org.example.todoapp.todos.mapper.TodoMapper;
import org.example.todoapp.user.authority.UserRole;
import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserDetails;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;
    private final CustomUserRepository userRepository;

    @Autowired
    public TodoController(TodoService todoService, CustomUserRepository userRepository) {
        this.todoService = todoService;
        this.userRepository = userRepository;
    }

    // ================= GET Todos för inloggad user =================
    @GetMapping
    public ResponseEntity<List<Todo>> getUserTodos(@AuthenticationPrincipal CustomUser user) {
        List<Todo> todos = todoService.getTodosForUser(user);
        return ResponseEntity.ok(todos);
    }

    // ================= POST ny Todo för user =================
    @PostMapping
    public ResponseEntity<Todo> createTodo(@AuthenticationPrincipal CustomUser user,
                                           @RequestBody Todo todo) {
        todo.setUser(user);
        Todo savedTodo = todoService.createTodo(todo);
        return ResponseEntity.ok(savedTodo);
    }

    // ================= GET ALL Todos (Admin only) =================
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        return ResponseEntity.ok(todos);
    }

    // ================= UPDATE Todo =================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@AuthenticationPrincipal CustomUser user,
                                        @PathVariable UUID id,
                                        @RequestBody Todo updatedTodo) {
        Optional<Todo> optionalTodo = todoService.getAllTodos().stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst();

        if (optionalTodo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Todo todo = optionalTodo.get();

        // Kontrollera om user är ägare eller admin
        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(UserRole.ADMIN)) {
            return ResponseEntity.status(403).body("Forbidden: You cannot update this todo");
        }

        // Uppdatera fält
        todo.setTitle(updatedTodo.getTitle());
        todo.setDescription(updatedTodo.getDescription());
        todo.setCompleted(updatedTodo.isCompleted());

        Todo savedTodo = todoService.updateTodo(todo);
        return ResponseEntity.ok(savedTodo);
    }

    // ================= DELETE Todo =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal CustomUser user,
                                        @PathVariable UUID id) {
        Optional<Todo> optionalTodo = todoService.getAllTodos().stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst();

        if (optionalTodo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Todo todo = optionalTodo.get();

        // Kontrollera om user är ägare eller admin
        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(UserRole.ADMIN)) {
            return ResponseEntity.status(403).body("Forbidden: You cannot delete this todo");
        }

        todoService.deleteTodo(id);
        return ResponseEntity.ok("Todo deleted successfully");
    }
}
