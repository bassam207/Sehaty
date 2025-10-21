package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserDTO {

    private String name;

    private String email;

    private String password;

    private String profileImageUrl;
}
