package com.Sehaty.Sehaty.mapper;


import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.model.SharedRecords;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Mapper class for converting SharedRecords entities to DTOs.
 */
@Component
public class ShareRecordMapper {

    /**
     * Converts a SharedRecords entity to a SharedRecordDTO.
     * Calculates time remaining until expiration and maps shared files.
     *
     * @param savedShare The SharedRecords entity.
     * @return The SharedRecordDTO.
     */
    public SharedRecordDTO toDTO(SharedRecords savedShare)
    {
        SharedRecordDTO responseDTO = new SharedRecordDTO();
        responseDTO.setId(savedShare.getId());
        responseDTO.setQrCode(savedShare.getQrCode());
        responseDTO.setSharedAt(savedShare.getSharedAt());
        responseDTO.setExpiresAt(savedShare.getExpiresAt());

        long timeRemaining = ChronoUnit.MINUTES.between(
                LocalDateTime.now(),
                savedShare.getExpiresAt()
        );
        responseDTO.setTimeRemaining(Math.max(0, timeRemaining));

        responseDTO.setStatus(savedShare.getStatus());


        List<MedicalFileResponseDTO> files = savedShare.getSharedFiles().stream().map(
             file -> {
                 MedicalFileResponseDTO fileResponseDTO = new MedicalFileResponseDTO();
                 fileResponseDTO.setId(file.getId());
                 fileResponseDTO.setCategory(file.getCategory() != null ? file.getCategory().getArabicName() : null);
                 fileResponseDTO.setSubCategory(file.getSubCategory());
                 fileResponseDTO.setDisplayName(file.getFileName());
                 fileResponseDTO.setUrl(file.getUrl());

                 return fileResponseDTO;

             }).toList();
        responseDTO.setSharedFiles(files);

        return responseDTO;

    }
}
