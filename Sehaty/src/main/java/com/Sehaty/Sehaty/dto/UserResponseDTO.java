package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    /** Full name of the user (patient or doctor). */
    private String name;

    /** User's email address, used for login and communication. */
    private String email;

    /** files of the patient.*/
    private List<MedicalFileResponseDTO> files;
}
