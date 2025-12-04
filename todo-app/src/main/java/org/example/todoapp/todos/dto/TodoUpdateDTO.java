package org.example.todoapp.todos.dto;

import jakarta.validation.constraints.Size;

public record TodoUpdateDTO(
        @Size(max = 50, message = "Title may not exceed 50 characters")
        String title,

        @Size(max = 250, message = "Description may not exceed 250 characters")
        String description,

        Boolean completed
) {}
