package org.example.todoapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** CustomUserResponseDTO
 *   Exposes username field only.
 *   Use CustomUserResponseDTO to hide sensitive information.
 *   TODO - Repeating Code, breaks DRY principle. Check CustomUserCreationDTO for duplicate code
 * */

public record CustomUserResponseDTO(
        @Size(min = 2, max = 25, message = "Username length should be between 2-25")
        @NotBlank(message = "Username may not contain whitespace characters only")
        String username
) {
}
