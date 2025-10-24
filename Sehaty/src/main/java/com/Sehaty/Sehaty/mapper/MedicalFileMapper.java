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
                file.getCategory(),
                file.getSubCategory(),
                file.getUrl()

        );
    }

    public MedicalFile toMedicalFile(MedicalFileUploadRequestDTO requestDTO)
    {
        return new MedicalFile(

                requestDTO.getCategory(),
                requestDTO.getSubCategory()
        );
    }
}
