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
    private LocalDateTime uploadDate;

    public MedicalFileResponseDTO(UUID id , String category, String subCategory , String fileName ,String url)
    {
        this.id = id;
        this.category  = category;
        this.subCategory = subCategory;
        this.fileName = fileName;
        this.url = url;
    }

}
