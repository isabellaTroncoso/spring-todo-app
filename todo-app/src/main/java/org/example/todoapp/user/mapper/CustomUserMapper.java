package org.example.todoapp.user.mapper;

import org.example.todoapp.user.custom.CustomUser;
import org.example.todoapp.user.dto.CustomUserCreationDTO;
import org.example.todoapp.user.dto.CustomUserResponseDTO;
import org.springframework.stereotype.Component;

/** CustomUserMapper:
 *   Converts CustomUser to Entity.
 *   Converts Entity to UsernameDTO
 * */

/*  Mapper: konverterar mellan DTO och entity
 Exempel: skapa CustomUser från CustomUserCreationDTO */

@Component
public class CustomUserMapper {

    public CustomUser toEntity(CustomUserCreationDTO customUserCreationDTO) {

        return new CustomUser(
                customUserCreationDTO.username(),
                customUserCreationDTO.password(),
                customUserCreationDTO.isAccountNonExpired(),
                customUserCreationDTO.isAccountNonLocked(),
                customUserCreationDTO.isCredentialsNonExpired(),
                customUserCreationDTO.isEnabled(),
                customUserCreationDTO.roles()
        );
    }

    public CustomUserResponseDTO toUsernameDTO(CustomUser customUser) {

        return new CustomUserResponseDTO(customUser.getUsername());
    }

}
