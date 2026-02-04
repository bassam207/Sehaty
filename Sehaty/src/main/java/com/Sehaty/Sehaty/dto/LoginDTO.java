package com.Sehaty.Sehaty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user login requests.
 * Contains the credentials required for authentication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDTO {

    /**
     * The user's email address.
     * Must be a valid email format and cannot be blank.
     */
    @NotBlank(message = "الايميل مطلوب ")
    @Email(message = "يرجي ادخال صيغة ايميل صالحة")
    private String email;

    /**
     * The user's password.
     * Cannot be blank.
     */
    @NotBlank(message = "كلمة السر مطلوبة ")
    private String password;
}
