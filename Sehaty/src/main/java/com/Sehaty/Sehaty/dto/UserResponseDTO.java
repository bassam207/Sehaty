package com.Sehaty.Sehaty.dto;

import com.Sehaty.Sehaty.shared.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private UUID id;
    /** Full name of the user (patient or doctor). */
    private String name;

    private LocalDate dateOfBirth;



    private int age;

    private Gender gender;

    /** User's email address, used for login and communication. */
    private String email;

    /** files of the patient.*/
    private List<MedicalFileResponseDTO> files;
}
