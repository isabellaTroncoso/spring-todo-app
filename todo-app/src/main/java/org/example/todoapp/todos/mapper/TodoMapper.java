package org.example.todoapp.todos.mapper;


import org.example.todoapp.todos.Todo;
import org.example.todoapp.todos.dto.TodoCreateDTO;
import org.example.todoapp.todos.dto.TodoResponseDTO;
import org.example.todoapp.todos.dto.TodoUpdateDTO;
import org.springframework.stereotype.Component;

@Component
public class TodoMapper {

    public Todo toEntity(TodoCreateDTO dto, org.example.todoapp.user.custom.CustomUser user) {
        return new Todo(dto.title(), dto.description(), false, user);
    }

    public void updateEntity(Todo todo, TodoUpdateDTO dto) {
        if (dto.title() != null) todo.setTitle(dto.title());
        if (dto.description() != null) todo.setDescription(dto.description());
        if (dto.completed() != null) todo.setCompleted(dto.completed());
    }

    public TodoResponseDTO toDTO(Todo todo) {
        return new TodoResponseDTO(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getUser().getUsername()
        );
    }
}
