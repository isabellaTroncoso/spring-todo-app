package org.example.todoapp.user.custom;
import jakarta.persistence.*;
import org.example.todoapp.user.authority.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

/** Entity - Separation of Concerns
 * This SHOULD NOT implement UserDetails
 * Handle UserDetails separately as its own class for better SoC
 * Should however, reflect UserDetails Variables as best practice
 * */

// CustomUser: representerar användare i databasen

@Table(name = "users")
@Entity
public class CustomUser {

    /** UUID
     * + Harder to accidentally expose
     * + Scales better in Global Apps (non-monolithic)
     * + Unique serial Key
     * - Harder to debug
     * - 16 bytes (2x larger than Long)
     * */
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER) // Fetch Immediately
    @Enumerated(value = EnumType.STRING)
    private Set<UserRole> roles;

    // Constructors
    public CustomUser() {}
    public CustomUser(String username, String password, boolean isAccountNonExpired, boolean isAccountNonLocked, boolean isCredentialsNonExpired, boolean isEnabled, Set<UserRole> roles) {
        this.username = username;
        this.password = password;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public void setUserRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    // TODO - Param injection, alternatives that are smoother that doesn't require manual param passing
    public void setPassword(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        isAccountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        isAccountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        isCredentialsNonExpired = credentialsNonExpired;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
}