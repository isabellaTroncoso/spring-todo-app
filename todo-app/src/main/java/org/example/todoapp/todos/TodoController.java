package org.example.todoapp.todos;

import jakarta.validation.Valid;
import org.example.todoapp.todos.dto.TodoCreateDTO;
import org.example.todoapp.todos.dto.TodoResponseDTO;
import org.example.todoapp.todos.dto.TodoUpdateDTO;
import org.example.todoapp.todos.mapper.TodoMapper;
import org.example.todoapp.user.custom.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;
    private final TodoMapper todoMapper;

    public TodoController(TodoService todoService, TodoMapper todoMapper) {
        this.todoService = todoService;
        this.todoMapper = todoMapper;
    }

    @PostMapping
    public ResponseEntity<TodoResponseDTO> createTodo(
            @Valid @RequestBody TodoCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Todo todo = todoMapper.toEntity(dto, currentUser.getCustomUser());
        Todo saved = todoService.createTodo(todo);
        return ResponseEntity.ok(todoMapper.toDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<TodoResponseDTO>> getTodos(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<TodoResponseDTO> todos = todoService.getTodosByUser(currentUser.getCustomUser())
                .stream().map(todoMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponseDTO> getTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return todoService.getTodoById(id)
                .filter(todo -> todo.getUser().getId().equals(currentUser.getCustomUser().getId()))
                .map(todoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponseDTO> updateTodo(
            @PathVariable UUID id,
            @Valid @RequestBody TodoUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return todoService.getTodoById(id)
                .filter(todo -> todo.getUser().getId().equals(currentUser.getCustomUser().getId()))
                .map(todo -> {
                    todoMapper.updateEntity(todo, dto);
                    return todoService.createTodo(todo);
                })
                .map(todoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        var todoOpt = todoService.getTodoById(id);

        if (todoOpt.isPresent() && todoOpt.get().getUser().getId().equals(currentUser.getCustomUser().getId())) {
            todoService.deleteTodo(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).build();
        }
    }
}
