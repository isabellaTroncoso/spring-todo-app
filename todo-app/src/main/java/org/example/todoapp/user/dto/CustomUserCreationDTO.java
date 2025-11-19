package org.example.todoapp.user.dto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.example.todoapp.user.authority.UserRole;

import java.util.Set;

/* DTO: används för att skicka/ta emot data från frontend,
 Exempel: CustomUserLoginDTO innehåller bara username och password */

public record CustomUserCreationDTO(

        @Size(min = 2, max = 25, message = "Username length should be between 2-25")
        @NotBlank(message = "Username may not contain whitespace characters only")
        String username,

        @Pattern(
                regexp = "^" +
                        "(?=.*[a-z])" +        // at least one lowercase letter
                        "(?=.*[A-Z])" +        // at least one uppercase letter
                        "(?=.*[0-9])" +        // at least one digit
                        "(?=.*[ @$!%*?&])" +   // at least one special character
                        ".+$",                 // one or more characters, until end
                message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character"
        )
        @Size(max = 80, message = "Maximum length of password exceeded")
        String password,

        @NotNull boolean isAccountNonExpired,
        @NotNull boolean isAccountNonLocked,
        @NotNull boolean isCredentialsNonExpired,
        @NotNull boolean isEnabled,
        // @NotNull @AssertTrue boolean acceptAppTerms, // Expect the result NOT to be null, NOT to be False

        @NotEmpty // Map, Collections, Array
        @Pattern(
                regexp = "^(GUEST|USER|ADMIN)$",
                message = "Must be a Valid Role"
        )
        Set<UserRole> roles

) {
}