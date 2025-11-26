package org.example.todoapp.todos;


import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.custom.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

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

    // GET all todos för inloggad user
    @GetMapping
    public ResponseEntity<List<Todo>> getUserTodos(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(todoService.getTodosForUser(user));
    }

    // CREATE todo
    @PostMapping
    public ResponseEntity<Todo> createTodo(@AuthenticationPrincipal CustomUser user,
                                           @RequestBody Todo todo) {
        todo.setUser(user);
        return ResponseEntity.ok(todoService.createTodo(todo));
    }

    // UPDATE todo
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable UUID id,
                                        @AuthenticationPrincipal CustomUser user,
                                        @RequestBody Todo updatedTodo) {
        Todo savedTodo = todoService.updateTodoIfAllowed(id, updatedTodo, user);
        if (savedTodo == null) return ResponseEntity.status(403).body("Forbidden or not found");
        return ResponseEntity.ok(savedTodo);
    }

    // DELETE todo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable UUID id,
                                        @AuthenticationPrincipal CustomUser user) {
        todoService.deleteTodoIfAllowed(id, user);
        return ResponseEntity.ok("Deleted");
    }

    // TOGGLE COMPLETE
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTodo(@PathVariable UUID id,
                                        @AuthenticationPrincipal CustomUser user) {
        todoService.toggleTodoCompleted(id, user);
        return ResponseEntity.ok("Toggled");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Todo>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

}
