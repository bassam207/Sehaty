package com.Sehaty.Sehaty.dto;


import com.Sehaty.Sehaty.model.MedicalFile;
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
    private String displayName;

    private String category;
    private String subCategory;

}
