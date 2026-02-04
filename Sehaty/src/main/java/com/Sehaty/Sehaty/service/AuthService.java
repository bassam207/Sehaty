package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.dto.AuthResponseDTO;
import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.exception.*;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.mapper.UserMapper;
import com.Sehaty.Sehaty.model.Token;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.TokenRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Service class for handling authentication logic.
 * Includes registration, login, and token management.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MedicalFileMapper medicalFileMapper;
    private final UserMapper userMapper;
    private final TokenRepository tokenRepository;

    /**
     * Registers a new user.
     * Validates input, creates a new user entity, and generates an authentication token.
     *
     * @param request DTO containing new user information.
     * @return AuthResponseDTO containing the JWT token and user details.
     * @throws InvalidEmailException if the email format is invalid.
     * @throws EmailAlreadyUsedException if the email is already registered.
     * @throws InvalidPasswordException if the password is too short.
     */
    public AuthResponseDTO register(UserRequestDTO request)
    {
        // ✅ Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        if (!request.getEmail().matches(emailRegex)) {
            throw new InvalidEmailException("الايميل غير صالح");
        }

        // ✅ Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("الايميل مستخدم بالفعل");
        }

        // ✅ Check password length
        if (request.getPassword().length() < 8) {
            throw new InvalidPasswordException("كلمة السر لازم تكون 8 حروف على الأقل");
        }

        // ✅ Check age >= 18
        validateAge(request.getDateOfBirth());

        // ✅ Map DTO to Entity
        User user = userMapper.convertToUser(request);
        if (user == null) {
            throw new NullPointerException("User mapping returned null");
        }

        // ✅ Hash password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ✅ Save user
        User savedUser = userRepository.save(user);

        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        // ✅ Return AuthResponseDTO
        return AuthResponseDTO.builder()
                .token(token)
                .userResponseDTO(userMapper.convertTOUserResponseDTO(savedUser))
                .build();
    }


    /**
     * Authenticates a user.
     * Verifies credentials and generates a new JWT token.
     *
     * @param loginDTO DTO containing login credentials.
     * @return AuthResponseDTO containing the JWT token and user details.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws BadRequestException if the password is incorrect.
     */
    public AuthResponseDTO loginUser(LoginDTO loginDTO)
    {
        // ✅ Check if email exists
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("الايميل او كلمة السر خطأ"));

        // ✅ Verify password
        boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!matches) {
            throw new BadRequestException("الايميل او كلمة السر خطأ");
        }

        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(user);

        Token tokenEntity = Token.builder()
                .token(token)
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(tokenEntity);

        // ✅ Return AuthResponseDTO
        return AuthResponseDTO.builder()
                .token(token)
                .userResponseDTO(userMapper.convertTOUserResponseDTO(user))
                .build();
    }

    /**
     * Validates the user's age.
     * Ensures the user is at least 18 years old and the date is reasonable.
     *
     * @param dob Date of birth.
     * @throws IllegalArgumentException if the age is invalid.
     */
    public void validateAge(LocalDate dob) {

        // Minimum age = 18 years
        LocalDate minDate = LocalDate.now().minusYears(18);

        // Maximum age = 120 years
        LocalDate maxDate = LocalDate.now().minusYears(120);

        if (dob.isAfter(minDate)) {
            throw new IllegalArgumentException("لازم عمر المستخدم يكون 18 سنة علي الاقل");
        }

        if (dob.isBefore(maxDate)) {
            throw new IllegalArgumentException("تاريخ الميلاد غير منطقي");
        }
    }

}
