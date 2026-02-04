package com.Sehaty.Sehaty.dto;

import com.Sehaty.Sehaty.shared.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for returning user information.
 * Used to send user details to the client without exposing sensitive data like passwords.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    /** Unique identifier of the user. */
    private UUID id;

    /** Full name of the user. */
    private String name;

    /** Date of birth of the user. */
    private LocalDate dateOfBirth;

    /** Calculated age of the user. */
    private int age;

    /** Gender of the user. */
    private Gender gender;

    /** User's email address. */
    private String email;

    /** List of medical files associated with the user. */
    private List<MedicalFileResponseDTO> files;
}
