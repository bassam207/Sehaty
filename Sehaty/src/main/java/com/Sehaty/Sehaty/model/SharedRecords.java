package com.Sehaty.Sehaty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a record of shared medical files.
 *
 * Each SharedRecord belongs to one User (the patient who shared the files),
 * and can contain multiple MedicalFile entries.
 *
 * The record also stores a unique QR code and expiration information,
 * allowing secure and time-limited access to medical data.
 */
@Entity
@Table(name = "shared_records")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class SharedRecords {

    /** Unique identifier for each share record. */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * The QR code (or its string representation) that allows others to access this share.
     */
    private String qrCode;

    private String qrUrl;

    /** Timestamp when the sharing was initiated. */
    private LocalDateTime sharedAt = LocalDateTime.now();

    /** Optional expiration time — after which access is disabled. */
    private LocalDateTime expiresAt;

    /**
     * Current status of the share.
     * Can be ACTIVE, EXPIRED, or REVOKED.
     */
    @Enumerated(EnumType.STRING)
    private ShareStatus status = ShareStatus.ACTIVE;

    /**
     * The user (typically the patient) who created the share.
     *
     * Relationship: Many SharedRecords → One User.
     *
     * - @ManyToOne → Defines the relationship.
     * - @JoinColumn(name = "user_id") → Adds a foreign key in the shared_records table.
     */

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user ;

    /**
     * List of medical files that were shared in this record.
     *
     * Relationship: Many SharedRecords ↔ Many MedicalFiles.
     *
     * A join table "share_files" is created automatically to hold the relationship.
     */

    @ManyToMany
    @JoinTable
            (
                    name = "share_files",
                    joinColumns = @JoinColumn(name = "share_id"),
                    inverseJoinColumns = @JoinColumn(name = "file_id")
            )
    private List<MedicalFile> sharedFiles = new ArrayList<>();

    /** Enumeration representing the status of a shared record. */
    public enum ShareStatus {
        ACTIVE,
        EXPIRED,
        REVOKED
    }
}
