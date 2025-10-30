package com.Sehaty.Sehaty.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    /** Full name of the user (patient or doctor). */
    @NotBlank(message = "الاسم مطلوب ")
    private String name;

    /** User's email address, used for login and communication. */
    @NotBlank(message = "الايميل مطلوب ")
    @Email(message = "يرجي ادخال صيغة ايميل صالحة")
    private String email;

    /** Encrypted password for authentication. */
    @NotBlank(message = "كلمة السر مطلوبة ")
    @Size(min = 8,message = "كلمة السر لازم تكون 8 احرف علي الاقل")
    private String password;


}
