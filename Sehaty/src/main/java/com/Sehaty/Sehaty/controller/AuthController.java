package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.AuthResponseDTO;
import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.service.AuthService;
import com.Sehaty.Sehaty.service.UserService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم التسجيل بنجاح", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = authService.loginUser(loginDTO);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم تسجيل الدخول بنجاح", response)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully.");
    }
}
