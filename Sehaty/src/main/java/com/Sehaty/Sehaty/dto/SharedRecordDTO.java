package com.Sehaty.Sehaty.dto;

import com.Sehaty.Sehaty.model.SharedRecords;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for shared medical records.
 * Contains information about a sharing session, including QR code details and shared files.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedRecordDTO {

    /** Unique identifier for the shared record. */
    private UUID id;

    /** The QR code string identifier (UUID). */
    private String qrCode;

    /** The data encoded in the QR code (e.g., URL to the shared view). */
    private String qrData;

    /** The full URL for accessing the shared record. */
    private String shareUrl;

    /** The name of the user sharing the record. */
    private String userName;

    /** The timestamp when the record was shared. */
    private LocalDateTime sharedAt;

    /** The timestamp when the sharing session expires. */
    private LocalDateTime expiresAt;

    /** The remaining time in minutes until expiration. */
    private long timeRemaining;

    /** The current status of the sharing session (ACTIVE, EXPIRED, REVOKED). */
    private SharedRecords.ShareStatus status;

    /** The list of medical files included in this shared record. */
    private List<MedicalFileResponseDTO> sharedFiles;

    /** Base64 encoded string of the QR code image. */
    private String qrImageBase64;
}
