package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateUserDTO {

    private String name;

    private String email;

    private String password;

    private String profileImageUrl;

    public UpdateUserDTO(String name , String email , String password)
    {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
