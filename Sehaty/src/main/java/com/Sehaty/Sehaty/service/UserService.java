package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.dto.UpdateUserDTO;
import com.Sehaty.Sehaty.exception.*;
import com.Sehaty.Sehaty.shared.ApiResponse;
import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.mapper.UserMapper;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    /**
     * Registers a new user.
     *
     * @param request DTO containing new user information
     * @return DTO containing saved user info (excluding sensitive data)
     */

    public UserResponseDTO register(UserRequestDTO request)
    {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        if(!request.getEmail().matches(emailRegex))
        {
            throw new InvalidEmailException("الايميل غير صالح");
        }

        if (userRepository.existsByEmail(request.getEmail()))
        {
            throw new EmailAlreadyUsedException("الايميل مستخدم بالفعل");
        }

        if(request.getPassword().length() <= 8)
        {
            throw new InvalidPasswordException("كلمة السر لازم تكون 8 حروف على الأقل");
        }

        User user = userMapper.convertToUser(request);
        String hashed = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashed);

        User savedUser = userRepository.save(user);
        return userMapper.convertTOUserResponseDTO(savedUser);
    }


    public UserResponseDTO loginUser(LoginDTO loginDTO)
    {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("الايميل او كلمة السر خطأ"));

        boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!matches)
        {
            throw new BadRequestException("الايميل او كلمة السر خطأ");
        }

        // ممكن ترجّع بيانات المستخدم بعد تسجيل الدخول
        return userMapper.convertTOUserResponseDTO(user);
    }


    public UserResponseDTO updateUser(UUID userId, UpdateUserDTO updateUserDTO)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        if (updateUserDTO.getName() != null)
        {
            user.setName(updateUserDTO.getName());
        }

        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().equals(user.getEmail()))
        {
            if(userRepository.existsByEmail(updateUserDTO.getEmail()))
            {
                throw new EmailAlreadyUsedException("الايميل مستخدم بالفعل");
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank())
        {
            if (updateUserDTO.getPassword().length() < 8)
            {
                throw new InvalidPasswordException("كلمة السر لازم تكون 8 حروف على الأقل");
            }
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.convertTOUserResponseDTO(updatedUser);
    }


    public UserResponseDTO getCurrentUser(UUID userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        return userMapper.convertTOUserResponseDTO(user);
    }





}
