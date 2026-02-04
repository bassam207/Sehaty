package com.Sehaty.Sehaty;


import com.Sehaty.Sehaty.dto.UpdateUserDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.exception.EmailAlreadyUsedException;
import com.Sehaty.Sehaty.exception.InvalidPasswordException;
import com.Sehaty.Sehaty.exception.ResourceNotFoundException;
import com.Sehaty.Sehaty.mapper.UserMapper;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.security.JwtUtil;
import com.Sehaty.Sehaty.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private  UserRepository userRepository;

    @Mock
    private  MedicalFileRepository medicalFileRepository;

    @Mock
    private  UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private  JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;
    private User user;
    private Authentication auth;

    @BeforeEach
    void setUp()
    {
        user = new User();
        user.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        user.setName("oldName");
        user.setEmail("old@gmail.com");
        user.setPassword("oldPassword");

        auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("123e4567-e89b-12d3-a456-426614174000");

    }

    @Nested
    @DisplayName("update user tests")
    class UpdateUserTests{


        @Test
        @DisplayName("Given user not founed , when updating account, then throw Rescource not found exception")
        void givenUserNotFound_whenUpdate_thenThrow()
        {
            //Given
            when(userRepository.findById(any())).thenReturn(Optional.empty());

            //When
            UpdateUserDTO dto = new UpdateUserDTO();

            //Then
            Assertions.assertThrows(ResourceNotFoundException.class,() ->userService.updateUser(auth,dto));
        }


        @Test
        @DisplayName("Given valid new email , when updating  , then email is updated ")
        void givenAvailableEmail_whenUpdate_thenEmailUpdated()
        {

            //Given

            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.convertTOUserResponseDTO(any())).thenReturn(new UserResponseDTO());

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setEmail("new@gmail.com");
            //When

            userService.updateUser(auth,dto);
            //Then
            Assertions.assertEquals("new@gmail.com",user.getEmail());
        }

        @Test
        @DisplayName("Given used email, when updating , then throw EmailUsed exception")
        void givenUsedEmail_whenUpdate_thenThrow()
        {
            //Given
            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@gmail.com")).thenReturn(true);

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setEmail("new@gmail.com");

            //When
            //Then

            Assertions.assertThrows(EmailAlreadyUsedException.class,()-> userService.updateUser(auth,dto));
        }

        @Test
        @DisplayName("Given short password, When updating, Then throw InvalidPasswordException")
        void givenShortPassword_whenUpdate_thenThrow()
        {
            //Given
                when(userRepository.findById(any())).thenReturn(Optional.of(user));

                UpdateUserDTO dto = new UpdateUserDTO();
                dto.setPassword("123");
            //When
            //Then
            Assertions.assertThrows(InvalidPasswordException.class,()-> userService.updateUser(auth,dto));
        }

        @Test
        @DisplayName("Given valid password, When updating, Then password is encoded and updated")
        void givenValidPassword_whenUpdate_thenPasswordUpdated() {

            //Given
            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newPassword")).thenReturn("done");
            when(userMapper.convertTOUserResponseDTO(any())).thenReturn(new UserResponseDTO());

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setPassword("newPassword");

            //When
            userService.updateUser(auth, dto);
            //Then
            Assertions.assertEquals("done",user.getPassword());
        }
    }

}
