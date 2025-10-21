package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.LoginDTO;
import com.Sehaty.Sehaty.dto.UpdateUserDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.service.UserService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Register a new user
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO user = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم التسجيل بنجاح", user));
    }

    /**
     * Login user
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserResponseDTO user = userService.loginUser(loginDTO);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم تسجيل الدخول بنجاح", user)
        );
    }

    /**
     * Update user information
     * PUT /api/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        UserResponseDTO user = userService.updateUser(userId, updateUserDTO);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم تحديث البيانات بنجاح", user)
        );
    }

    /**
     * Get current user information
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getCurrentUser(@PathVariable UUID userId) {
        UserResponseDTO user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم جلب بيانات المستخدم بنجاح", user)
        );
    }
}
