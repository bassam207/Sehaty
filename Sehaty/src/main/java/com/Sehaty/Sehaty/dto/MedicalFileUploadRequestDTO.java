package com.Sehaty.Sehaty.dto;


import com.Sehaty.Sehaty.model.MedicalFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for medical file upload requests.
 * Contains metadata required when uploading a new medical file.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalFileUploadRequestDTO {

    /** Display name for the file being uploaded. */
    private String displayName;

    /** Category of the file (e.g., Radiology, Labs). */
    private String category;

    /** Sub-category of the file. */
    private String subCategory;

}
