package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating user information.
 * Allows partial updates to user profile details.
 */
@Data
@NoArgsConstructor
public class UpdateUserDTO {

    /** The new name of the user. */
    private String name;

    /** The new email address of the user. */
    private String email;

    /** The new password for the user. */
    private String password;

    /** The URL of the new profile image. */
    private String profileImageUrl;

    /**
     * Constructor to create an UpdateUserDTO with basic details.
     *
     * @param name     The new name
     * @param email    The new email
     * @param password The new password
     */
    public UpdateUserDTO(String name , String email , String password)
    {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
