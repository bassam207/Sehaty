package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AuthResponseDTO {

    private String token;

    private UserResponseDTO userResponseDTO;



}
