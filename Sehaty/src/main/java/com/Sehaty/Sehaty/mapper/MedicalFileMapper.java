package com.Sehaty.Sehaty.mapper;


import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.model.MedicalFile;
import org.springframework.stereotype.Component;

@Component
public class MedicalFileMapper {

    public MedicalFileResponseDTO toMedicalFileResponseDTO(MedicalFile file)
    {
        return new MedicalFileResponseDTO(
                file.getId(),
                file.getFileName(),
                file.getCategory().getArabicName(),
                file.getSubCategory(),
                file.getUrl()

        );
    }

    public MedicalFile toMedicalFile(MedicalFileUploadRequestDTO requestDTO)
    {
        MedicalFile.FileCategory categoryEnum = MedicalFile.FileCategory.fromArabic(requestDTO.getCategory());
        return new MedicalFile(categoryEnum, requestDTO.getSubCategory());
    }
}
