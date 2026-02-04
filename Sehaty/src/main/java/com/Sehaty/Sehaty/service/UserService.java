package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.audit.AuditLog;
import com.Sehaty.Sehaty.dto.*;
import com.Sehaty.Sehaty.exception.*;
import com.Sehaty.Sehaty.security.JwtUtil;
import com.Sehaty.Sehaty.shared.ApiResponse;
import com.Sehaty.Sehaty.mapper.UserMapper;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for managing user-related business logic in the Sehaty application.
 *
 * Responsibilities:
 * - Handle creation, update, and deletion of users.
 * - Retrieve user details and their associated medical files.
 * - Provide business-level validation and coordination between repositories.
 */
@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    private final MedicalFileRepository medicalFileRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;


    /**
     * Updates user information.
     * Allows updating name, email, and password.
     *
     * @param authentication The current authentication object.
     * @param updateUserDTO DTO containing updated user information.
     * @return UserResponseDTO with the updated user details.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws EmailAlreadyUsedException if the new email is already in use.
     * @throws InvalidPasswordException if the new password is invalid.
     */
    @CacheEvict(value = "users", key = "#authentication.name")
    @AuditLog(action = "UPDATE_USER")
    public UserResponseDTO updateUser(Authentication authentication, UpdateUserDTO updateUserDTO)
    {
        String userId = authentication.getName();

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        if (updateUserDTO.getName() != null) {
            user.setName(updateUserDTO.getName());
        }

        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().isBlank()) {
            if (!updateUserDTO.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(updateUserDTO.getEmail())) {
                throw new EmailAlreadyUsedException("الايميل مستخدم بالفعل");
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank()) {
            if (updateUserDTO.getPassword().length() < 8) {
                throw new InvalidPasswordException("كلمة السر لازم تكون 8 حروف على الأقل");
            }
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        return userMapper.convertTOUserResponseDTO(updatedUser);
    }


    /**
     * Retrieves the current authenticated user's details.
     *
     * @param authentication The current authentication object.
     * @return UserResponseDTO containing user details.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Cacheable(value = "users", key = "#authentication.name")
    @AuditLog(action = "GET_CURRENT_USER")
    public UserResponseDTO getCurrentUser(Authentication authentication)
    {
        String userId = authentication.getName();

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));
        return userMapper.convertTOUserResponseDTO(user);
    }


}
