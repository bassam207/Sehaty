package com.Sehaty.Sehaty.dto;




import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.SharedRecords;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedRecordDTO {

    private UUID id;
    private String qrCode;
    private String qrData;
    private String userName;
    private LocalDateTime sharedAt;
    private LocalDateTime expiresAt;
    private long timeRemaining;
    private SharedRecords.ShareStatus status;
    private List<MedicalFileResponseDTO> sharedFiles;
}
