package org.example.todoapp.user.authority;

/* Handles the ROLE_ concatenation
 *   This ENUM is essentially just a value holder
 * */

public enum UserRoleName {

    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String roleName;

    UserRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
