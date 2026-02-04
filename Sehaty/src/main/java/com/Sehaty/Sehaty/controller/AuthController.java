package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.AuthResponseDTO;
import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.service.AuthService;
import com.Sehaty.Sehaty.service.LogoutService;
import com.Sehaty.Sehaty.service.UserService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling authentication-related endpoints.
 * Includes user registration, login, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;


    /**
     * Registers a new user.
     *
     * @param request DTO containing user registration information.
     * @return ApiResponse with authentication details upon successful registration.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم التسجيل بنجاح", response));
    }

    /**
     * Authenticates a user and provides a JWT token.
     *
     * @param loginDTO DTO containing user login credentials.
     * @return ApiResponse with authentication details upon successful login.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = authService.loginUser(loginDTO);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم تسجيل الدخول بنجاح", response)
        );
    }

    /**
     * Logs out the current user by invalidating their token.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param authentication The current authentication object.
     * @return A confirmation message upon successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) {
        // ✅ Call the logout service
        logoutService.logout(request, response, authentication);
        return ResponseEntity.ok("Logged out successfully.");
    }}
