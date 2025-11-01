package com.Sehaty.Sehaty.dto;


import com.Sehaty.Sehaty.shared.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    /** Full name of the user (patient or doctor). */
    @NotBlank(message = "الاسم مطلوب ")
    private String name;


    @NotNull(message = "تحديد الجنس مطلوب")
    private Gender gender;

    @NotNull(message = "العمر مطلوب لفهم حالتك الطبية")
    @Past(message = "تاريخ الميلاد لازم يكون  في الماضي")
    private LocalDate dateOfBirth;

    /** User's email address, used for login and communication. */
    @NotBlank(message = "الايميل مطلوب ")
    @Email(message = "يرجي ادخال صيغة ايميل صالحة")
    private String email;

    /** Encrypted password for authentication. */
    @NotBlank(message = "كلمة السر مطلوبة ")
    @Size(min = 8,message = "كلمة السر لازم تكون 8 احرف علي الاقل")
    private String password;


}
