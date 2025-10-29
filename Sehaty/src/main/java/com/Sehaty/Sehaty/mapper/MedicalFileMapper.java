package com.Sehaty.Sehaty.mapper;


import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.shared.FileCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class MedicalFileMapper {

    public MedicalFileResponseDTO toMedicalFileResponseDTO(MedicalFile file)
    {
        return new MedicalFileResponseDTO(
                file.getId(),
                file.getFileName(),
                file.getCategory() != null ? file.getCategory().getArabicName() : null,
                file.getSubCategory(),
                file.getUrl()

        );
    }

    public MedicalFile toMedicalFile(MedicalFileUploadRequestDTO requestDTO)
    {MedicalFile file = new MedicalFile();
        file.setCategory(parseCategory(requestDTO.getCategory())); // ← هنا التحويل الصحيح
        file.setSubCategory(requestDTO.getSubCategory());
        return file;
    }

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
