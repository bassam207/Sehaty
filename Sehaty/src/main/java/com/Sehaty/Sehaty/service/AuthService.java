package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.dto.AuthResponseDTO;
import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.exception.BadRequestException;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MedicalFileMapper medicalFileMapper;

    public AuthResponseDTO register(UserRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())     // نستخدم الإيميل كـ username
                .password(user.getPassword())
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(userDetails);

        UserResponseDTO userResponseDTO = new UserResponseDTO(

                user.getName(),
                user.getEmail(),
                user.getFiles().stream().map(medicalFileMapper::toMedicalFileResponseDTO).
                        collect(Collectors.toList())

        );

        return new AuthResponseDTO(token, userResponseDTO);
    }

    public AuthResponseDTO login(LoginDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())     // نستخدم الإيميل كـ username
                .password(user.getPassword())
                .build();



        String token = jwtUtil.generateToken(userDetails);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getFiles().stream().map(medicalFileMapper::toMedicalFileResponseDTO)
                        .collect(Collectors.toList())
        );
        return new AuthResponseDTO(token,userResponseDTO);
    }

}
