package com.Sehaty.Sehaty.dto;

import com.Sehaty.Sehaty.model.MedicalFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for returning medical file details.
 * Provides information about a specific medical file.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalFileResponseDTO {

    /** Unique identifier of the medical file. */
    private UUID id ;

    /** Display name of the file. */
    private String displayName;

    /** Category of the medical file (e.g., Radiology, Labs). */
    private String  category;

    /** Sub-category of the medical file. */
    private String subCategory;

    /** URL to access or download the file. */
    private String url;
}
