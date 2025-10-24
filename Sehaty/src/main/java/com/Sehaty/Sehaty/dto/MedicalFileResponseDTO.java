package com.Sehaty.Sehaty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalFileResponseDTO {

    private UUID id ;
    private String fileName;
    private String category;
    private String subCategory;
    private String url;




}
