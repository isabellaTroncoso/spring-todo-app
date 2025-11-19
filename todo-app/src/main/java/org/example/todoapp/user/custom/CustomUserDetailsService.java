package org.example.todoapp.user.custom;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** CustomUserDetailsService
 *      Loads the User from the database through CustomUserRepository
 *      Implements UserDetailsService - loadByUsername() override method
 *      Implements Business Logic & Error Handling (preferably through Advice) TODO
 * */

// CustomUserDetailsService: hämtar användare från DB vid login

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomUserRepository customUserRepository;

    @Autowired
    public CustomUserDetailsService(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        CustomUser customUser = customUserRepository.findUserByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User with username " + username + " Was not found")
                );

        // TODO - Possibility for MAPPING instead of Pushing an Entity within UserDetails

        return new CustomUserDetails(customUser); // CustomUserDetails contains an Entity
    }
}
