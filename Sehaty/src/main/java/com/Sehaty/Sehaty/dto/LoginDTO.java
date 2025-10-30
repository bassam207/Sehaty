package com.Sehaty.Sehaty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    @NotBlank(message = "الايميل مطلوب ")
    @Email(message = "يرجي ادخال صيغة ايميل صالحة")
    private String email;

    @NotBlank(message = "كلمة السر مطلوبة ")
    private String password;
}
