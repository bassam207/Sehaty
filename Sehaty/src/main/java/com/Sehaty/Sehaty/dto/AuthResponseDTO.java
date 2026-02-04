package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for authentication responses.
 * Returned after successful login or registration.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AuthResponseDTO {

    /** The JWT token generated for the authenticated session. */
    private String token;

    /** The user details associated with the authenticated session. */
    private UserResponseDTO userResponseDTO;

}
