package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.dto.AuthResponseDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.exception.EmailAlreadyUsedException;
import com.Sehaty.Sehaty.mapper.UserMapper;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.TokenRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.security.JwtUtil;
import com.Sehaty.Sehaty.shared.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private AuthService authService;

    private UserRequestDTO userRequestDTO;
    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userRequestDTO = UserRequestDTO.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(false);
        when(userMapper.convertToUser(any(UserRequestDTO.class))).thenReturn(user);
        when(passwordEncoder.encode(userRequestDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("test-token");
        when(userMapper.convertTOUserResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        AuthResponseDTO authResponseDTO = authService.register(userRequestDTO);

        assertNotNull(authResponseDTO);
        assertEquals("test-token", authResponseDTO.getToken());
        assertNotNull(authResponseDTO.getUserResponseDTO());
        assertEquals(user.getId(), authResponseDTO.getUserResponseDTO().getId());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class, () -> {
            authService.register(userRequestDTO);
        });
    }
}
