package org.example.todoapp.todos;
import jakarta.validation.Valid;
import org.example.todoapp.todos.dto.TodoCreateDTO;
import org.example.todoapp.todos.dto.TodoResponseDTO;
import org.example.todoapp.todos.dto.TodoUpdateDTO;
import org.example.todoapp.todos.mapper.TodoMapper;
import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserDetails;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;
    private final CustomUserRepository userRepository;
    private final TodoMapper todoMapper;

    @Autowired
    public TodoController(TodoService todoService,
                          CustomUserRepository userRepository,
                          TodoMapper todoMapper) {
        this.todoService = todoService;
        this.userRepository = userRepository;
        this.todoMapper = todoMapper;
    }

    // ==================== GET USER TODOS ====================
    @GetMapping
    public ResponseEntity<List<TodoResponseDTO>> getUserTodos(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CustomUser user = userDetails.getCustomUser();
        List<Todo> todos = todoService.getTodosForUser(user);
        List<TodoResponseDTO> dtoList = todos.stream()
                .map(todoMapper::toDTO)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    // ===================== CREATE TODO ======================
    @PostMapping
    public ResponseEntity<TodoResponseDTO> createTodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TodoCreateDTO dto) {

        CustomUser user = userDetails.getCustomUser();
        Todo todo = todoMapper.toEntity(dto, user);
        Todo saved = todoService.createTodo(todo);

        return ResponseEntity.ok(todoMapper.toDTO(saved));
    }

    // ===================== UPDATE TODO ======================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TodoUpdateDTO dto) {

        CustomUser user = userDetails.getCustomUser();
        Todo todo = todoService.getTodoById(id).orElse(null);
        if (todo == null) {
            return ResponseEntity.notFound().build();
        }

        // Security: must be owner OR admin
        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.isAdmin()) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        todoMapper.updateEntity(todo, dto);
        Todo saved = todoService.save(todo);

        return ResponseEntity.ok(todoMapper.toDTO(saved));
    }

    // ===================== DELETE TODO ======================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CustomUser user = userDetails.getCustomUser();
        Todo todo = todoService.getTodoById(id).orElse(null);
        if (todo == null) {
            return ResponseEntity.notFound().build();
        }

        // Security
        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        todoService.deleteTodoIfAllowed(id, user);

        // Skicka JSON istället för plain text
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }


    // =================== TOGGLE COMPLETE ====================
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CustomUser user = userDetails.getCustomUser();
        Todo todo = todoService.getTodoById(id).orElse(null);
        if (todo == null) {
            return ResponseEntity.notFound().build();
        }

        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.isAdmin()) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Todo updated = todoService.toggleTodoCompleted(id, user);
        return ResponseEntity.ok(todoMapper.toDTO(updated));
    }

    // =================== GET ALL TODOS (OPTIONAL ADMIN) ====================
    @GetMapping("/all")
    public ResponseEntity<?> getAllTodos(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CustomUser user = userDetails.getCustomUser();
        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        List<Todo> todos = todoService.getAllTodos();
        List<TodoResponseDTO> dtoList = todos.stream()
                .map(todoMapper::toDTO)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponseDTO> getTodoById(@PathVariable UUID id) {
        Optional<Todo> todo = todoService.getTodoById(id);
        if (todo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(todoMapper.toDTO(todo.get()));
    }



}