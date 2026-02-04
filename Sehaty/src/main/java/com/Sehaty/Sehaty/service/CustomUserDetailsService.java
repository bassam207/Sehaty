package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Custom implementation of UserDetailsService.
 * Loads user details from the database for authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by user ID (UUID).
     * Note: The parameter name is 'userId' but the method signature requires 'username'.
     * In this application, the username is the user's UUID.
     *
     * @param userId The user's UUID as a string.
     * @return UserDetails object containing user information.
     * @throws UsernameNotFoundException if the user is not found or the ID is invalid.
     */
    public UserDetails loadUserByUsername(String userId ) throws UsernameNotFoundException {

        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid userId in token");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())   // ✅ principal = userId
                .password(user.getPassword())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
}}
