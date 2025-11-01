package com.Sehaty.Sehaty.service;

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




    public UserResponseDTO updateUser(Authentication authentication, UpdateUserDTO updateUserDTO)
    {
        // ✅ 1. Extract the email (username) from the JWT
        String email = authentication.getName();

        // ✅ 2. Fetch the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        // ✅ 3. Update name if provided
        if (updateUserDTO.getName() != null) {
            user.setName(updateUserDTO.getName());
        }

        // ✅ 4. Update email if provided and not already used
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().isBlank()) {
            if (!updateUserDTO.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(updateUserDTO.getEmail())) {
                throw new EmailAlreadyUsedException("الايميل مستخدم بالفعل");
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        // ✅ 5. Update password if provided and valid
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank()) {
            if (updateUserDTO.getPassword().length() < 8) {
                throw new InvalidPasswordException("كلمة السر لازم تكون 8 حروف على الأقل");
            }
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        // ✅ 6. Save updates
        User updatedUser = userRepository.save(user);

        // ✅ 7. Convert to DTO and return
        return userMapper.convertTOUserResponseDTO(updatedUser);
    }


    public UserResponseDTO getCurrentUser(Authentication authentication)
    {
        String email = authentication.getName(); // email stored in token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));
        return userMapper.convertTOUserResponseDTO(user);
    }











}
