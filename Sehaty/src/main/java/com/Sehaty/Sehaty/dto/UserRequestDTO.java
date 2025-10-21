package com.Sehaty.Sehaty.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    /** Full name of the user (patient or doctor). */
    private String name;

    /** User's email address, used for login and communication. */
    private String email;

    /** Encrypted password for authentication. */
    private String password;
}
