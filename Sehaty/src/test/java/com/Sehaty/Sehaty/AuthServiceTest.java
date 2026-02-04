package com.Sehaty.Sehaty;

import com.Sehaty.Sehaty.dto.AuthResponseDTO;
import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.exception.*;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.mapper.UserMapper;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.TokenRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.security.JwtUtil;
import com.Sehaty.Sehaty.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserMapper userMapper;
    @Mock private TokenRepository tokenRepository;
    @Mock private MedicalFileMapper medicalFileMapper;

    @InjectMocks private AuthService authService;

    private UserRequestDTO validUserRequest;
    private User userEntity;
    private UserResponseDTO userResponseDTO;
    private LoginDTO validLoginDTO;
    private final String VALID_TOKEN = "jwt-valid-token";
    private final String ENCODED_PASSWORD = "encoded_password_hash";

    @BeforeEach
    void setUp() {
        validUserRequest = UserRequestDTO.builder()
                .email("test@example.com")
                .password("StrongPassword123")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .name("user")
                .build();

        userEntity = User.builder()
                .id(UUID.randomUUID())
                .email(validUserRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .name(validUserRequest.getName())
                .dateOfBirth(validUserRequest.getDateOfBirth())
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .email(userEntity.getEmail())
                .build();

        validLoginDTO = LoginDTO.builder()
                .email("test@example.com")
                .password("StrongPassword123")
                .build();
    }

    // ========================= REGISTER TESTS =========================
    @Test
    @DisplayName("Register Success returns token")
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.convertToUser(any())).thenReturn(userEntity);
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any())).thenReturn(userEntity);
        when(jwtUtil.generateToken(any())).thenReturn(VALID_TOKEN);
        when(userMapper.convertTOUserResponseDTO(any())).thenReturn(userResponseDTO);

        AuthResponseDTO response = authService.register(validUserRequest);

        assertNotNull(response);
        assertEquals(VALID_TOKEN, response.getToken());
        assertEquals(userResponseDTO.getEmail(), response.getUserResponseDTO().getEmail());

        verify(userRepository).existsByEmail(validUserRequest.getEmail());
        verify(userRepository).save(userEntity);
        verify(passwordEncoder).encode(validUserRequest.getPassword());
        verify(jwtUtil).generateToken(userEntity);
    }

    @Test
    @DisplayName("Register: invalid email throws InvalidEmailException")
    void register_InvalidEmail_ThrowsException() {
        validUserRequest.setEmail("invalid-email.com");

        InvalidEmailException thrown = assertThrows(InvalidEmailException.class,
                () -> authService.register(validUserRequest));

        assertEquals("الايميل غير صالح", thrown.getMessage());

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register: email exists throws EmailAlreadyUsedException")
    void register_EmailAlreadyUsed_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        EmailAlreadyUsedException thrown = assertThrows(EmailAlreadyUsedException.class,
                () -> authService.register(validUserRequest));

        assertEquals("الايميل مستخدم بالفعل", thrown.getMessage());
        verify(userRepository).existsByEmail(validUserRequest.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register: short password throws InvalidPasswordException")
    void register_ShortPassword_ThrowsException() {
        validUserRequest.setPassword("short");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        InvalidPasswordException thrown = assertThrows(InvalidPasswordException.class,
                () -> authService.register(validUserRequest));

        assertEquals("كلمة السر لازم تكون 8 حروف على الأقل", thrown.getMessage());
        verify(userRepository).existsByEmail(validUserRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Register: underage throws IllegalArgumentException")
    void register_UnderAge_ThrowsException() {
        validUserRequest.setDateOfBirth(LocalDate.now().minusYears(17));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> authService.register(validUserRequest));

        assertEquals("لازم عمر المستخدم يكون 18 سنة علي الاقل", thrown.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Register: mapper returns null throws NullPointerException")
    void register_MapperReturnsNull_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.convertToUser(any())).thenReturn(null);

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> authService.register(validUserRequest)
        );

        assertEquals("User mapping returned null", ex.getMessage());
    }

    // ========================= LOGIN TESTS =========================
    @Test
    @DisplayName("Login success returns token")
    void loginUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn(VALID_TOKEN);
        when(userMapper.convertTOUserResponseDTO(any())).thenReturn(userResponseDTO);

        AuthResponseDTO response = authService.loginUser(validLoginDTO);

        assertNotNull(response);
        assertEquals(VALID_TOKEN, response.getToken());
        assertEquals(userResponseDTO.getEmail(), response.getUserResponseDTO().getEmail());

        verify(userRepository).findByEmail(validLoginDTO.getEmail());
        verify(passwordEncoder).matches(validLoginDTO.getPassword(), userEntity.getPassword());
        verify(jwtUtil).generateToken(userEntity);
        verify(tokenRepository).save(any());
    }

    @Test
    @DisplayName("Login: email not found throws ResourceNotFoundException")
    void loginUser_EmailNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> authService.loginUser(validLoginDTO));

        assertEquals("الايميل او كلمة السر خطأ", thrown.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login: password mismatch throws BadRequestException")
    void loginUser_PasswordMismatch_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BadRequestException thrown = assertThrows(BadRequestException.class,
                () -> authService.loginUser(validLoginDTO));

        assertEquals("الايميل او كلمة السر خطأ", thrown.getMessage());
        verify(passwordEncoder).matches(validLoginDTO.getPassword(), userEntity.getPassword());
        verify(jwtUtil, never()).generateToken(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login: token save failure throws RuntimeException")
    void loginUser_TokenSaveFails_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn(VALID_TOKEN);
        when(tokenRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.loginUser(validLoginDTO));

        assertEquals("DB error", ex.getMessage());
    }

    // ========================= AGE VALIDATION =========================
    @Test
    @DisplayName("Validate age exactly 18 passes")
    void validateAge_Exactly18_Passes() {
        assertDoesNotThrow(() -> authService.validateAge(LocalDate.now().minusYears(18)));
    }

    @Test
    @DisplayName("Validate age over 18 passes")
    void validateAge_Over18_Passes() {
        assertDoesNotThrow(() -> authService.validateAge(LocalDate.now().minusYears(20)));
    }

    @Test
    @DisplayName("Validate age under 18 throws exception")
    void validateAge_Under18_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.validateAge(LocalDate.now().minusYears(17).plusDays(1))
        );
        assertEquals("لازم عمر المستخدم يكون 18 سنة علي الاقل", thrown.getMessage());
    }

    @Test
    @DisplayName("Validate age unrealistic (>120) throws exception")
    void validateAge_UnrealisticAge_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.validateAge(LocalDate.now().minusYears(150))
        );
        assertEquals("تاريخ الميلاد غير منطقي", thrown.getMessage());
    }
}