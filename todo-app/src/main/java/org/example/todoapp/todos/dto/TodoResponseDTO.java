package org.example.todoapp.todos.dto;
import java.util.UUID;

public record TodoResponseDTO(  UUID id,
                                String title,
                                String description,
                                boolean completed,
                                String username) {
}
