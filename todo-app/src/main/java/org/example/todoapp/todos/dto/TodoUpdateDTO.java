package org.example.todoapp.todos.dto;

import jakarta.validation.constraints.Size;

public record TodoUpdateDTO(@Size(max = 50) String title,
                            @Size(max = 250) String description,
                            Boolean completed) {
}
