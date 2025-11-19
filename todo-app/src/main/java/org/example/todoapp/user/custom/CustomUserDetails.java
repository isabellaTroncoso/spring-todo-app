package org.example.todoapp.user.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** UserDetails - Custom Implementation
 *   Why:
 *      - SoC, offload the CustomUser: Entity
 *      - Override for Custom Logic (e.g: loading authorities through Enum)
 *      - Spring handles Authentication Logic behind the scenes using UserDetails class
 *      - Entity preferably LIVES within CustomUserDetails
 * */

// CustomUserDetails: används av Spring Security för autentisering

public class CustomUserDetails implements UserDetails {

    private final CustomUser customUser;

    public CustomUserDetails(CustomUser customUser) {
        this.customUser = customUser;
    }

    /** UserRole.getAuthorities
     *      Returns: ROLE + PERMISSIONS [ROLE_ADMIN, READ, WRITE, DELETE]
     *      IF you have two roles... [[ROLE_ADMIN, READ, WRITE], [ROLE_GUEST]]
     *      List<List<SimpleGrantedAuthority>>
     * */


    public CustomUser getCustomUser() {
        return customUser;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        final Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        customUser.getRoles().forEach(
                userRole -> authorities.addAll(userRole.getUserAuthorities()) // Merge arrays
        );

        return Collections.unmodifiableSet(authorities); // Make List 'final' through 'unmodifiable'
    }

    @Override
    public String getPassword() {
        return customUser.getPassword();
    }

    @Override
    public String getUsername() {
        return customUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return customUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return customUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return customUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return customUser.isEnabled();
    }
}