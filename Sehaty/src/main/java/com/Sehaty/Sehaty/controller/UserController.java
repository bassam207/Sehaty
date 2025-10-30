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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;



    /**
     * Get current authenticated user (using JWT)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(Authentication authentication) {
        UserResponseDTO user = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم جلب بيانات المستخدم بنجاح", user)
        );
    }

    /**
     * Update current authenticated user (using JWT)
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse> updateUser(
            Authentication authentication,
            @RequestBody UpdateUserDTO updateUserDTO) {

        UserResponseDTO user = userService.updateUser(authentication, updateUserDTO);
        return ResponseEntity.ok(
                new ApiResponse(true, "تم تحديث البيانات بنجاح", user)
        );
    }
}
