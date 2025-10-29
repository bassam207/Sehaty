package com.Sehaty.Sehaty.dto;

import com.Sehaty.Sehaty.model.MedicalFile;
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
    private String displayName;

    private String  category;
    private String subCategory;
    private String url;




}
