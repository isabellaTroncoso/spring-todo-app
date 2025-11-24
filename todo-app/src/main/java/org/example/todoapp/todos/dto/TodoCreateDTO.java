package org.example.todoapp.todos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoCreateDTO(
        @NotBlank @Size(max = 50) String title,
        @Size(max = 250) String description
) {}
