package com.Sehaty.Sehaty.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalFileUploadRequestDTO {

    private MultipartFile file;
    private String fileName;
    private String category;
    private String subCategory;

}
