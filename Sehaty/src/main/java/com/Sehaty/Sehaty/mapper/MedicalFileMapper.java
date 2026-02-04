package com.Sehaty.Sehaty.mapper;


import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.shared.FileCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Mapper class for converting between MedicalFile entities and DTOs.
 */
@Component
public class MedicalFileMapper {

    /**
     * Converts a MedicalFile entity to a MedicalFileResponseDTO.
     *
     * @param file The MedicalFile entity.
     * @return The MedicalFileResponseDTO.
     */
    public MedicalFileResponseDTO toMedicalFileResponseDTO(MedicalFile file)
    {
        return new MedicalFileResponseDTO(
                file.getId(),
                file.getDisplayName(),
                file.getCategory() != null ? file.getCategory().getArabicName() : null,
                file.getSubCategory(),
                file.getUrl()

        );
    }

    /**
     * Converts a MedicalFileUploadRequestDTO to a MedicalFile entity.
     * Handles category parsing from string to Enum.
     *
     * @param requestDTO The DTO containing file upload data.
     * @return The MedicalFile entity.
     */
    public MedicalFile toMedicalFile(MedicalFileUploadRequestDTO requestDTO)
    {MedicalFile file = new MedicalFile();
        file.setCategory(parseCategory(requestDTO.getCategory())); // ← هنا التحويل الصحيح
        file.setSubCategory(requestDTO.getSubCategory());
        return file;
    }

    /**
     * Parses a category string into a FileCategory enum.
     * Supports both English names and Arabic display names.
     *
     * @param categoryName The category name string.
     * @return The matching FileCategory enum.
     * @throws IllegalArgumentException if the category is invalid.
     */
    private FileCategory parseCategory(String categoryName) {
        if (categoryName == null) return null;
        try {
            // لو الموبايل بيبعت الاسم بالإنجليزي
            return FileCategory.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // لو الموبايل بيبعت بالعربي
            return Arrays.stream(FileCategory.values())
                    .filter(c -> c.getArabicName().equalsIgnoreCase(categoryName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("فئة غير صحيحة: " + categoryName));
        }
    }
}
