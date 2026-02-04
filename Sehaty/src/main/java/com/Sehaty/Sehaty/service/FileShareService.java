package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.audit.AuditLog;
import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.exception.QRCodeGenerationException;
import com.Sehaty.Sehaty.exception.ResourceNotFoundException;
import com.Sehaty.Sehaty.exception.BadRequestException;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.mapper.ShareRecordMapper;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.SharedRecords;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.SharedRecordRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing file sharing sessions.
 * Handles creation, access, revocation, and retrieval of shared records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileShareService {

    private final UserRepository userRepository;
    private final MedicalFileRepository medicalFileRepository;
    private final SharedRecordRepository sharedRecordRepository;
    private final ShareRecordMapper shareRecordMapper;
    private final MedicalFileMapper medicalFileMapper;

    @Value("${sehaty.share.expiry-minutes}")
    private int shareExpiryMinutes;

    @Value("${app.frontend-url}")
    private String baseUrl;

    /**
     * Creates a new sharing session for selected files.
     * Generates a QR code and sets an expiration time.
     *
     * @param userDetails The authenticated user.
     * @param fileIds List of file IDs to share.
     * @return SharedRecordDTO containing session details.
     * @throws ResourceNotFoundException if user or files are not found.
     * @throws BadRequestException if files do not belong to the user.
     */
    @AuditLog(action = "CREATE_SHARE_SESSION")
    public SharedRecordDTO createShareSession(UserDetails userDetails, List<UUID> fileIds) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        List<MedicalFile> files = medicalFileRepository.findAllById(fileIds);
        if (files.isEmpty()) {
            throw new ResourceNotFoundException("لا توجد ملفات للمشاركة");
        }

        validateFilesOwnership(files, userId);

        String qrCode = UUID.randomUUID().toString();
        SharedRecords sharedRecords = createSharedRecord(user, files, qrCode);
        SharedRecords savedShare = sharedRecordRepository.save(sharedRecords);

        String qrData = baseUrl + "/share/by-qr?qrCode=" + qrCode;
        sharedRecords.setQrData(qrData);
        savedShare = sharedRecordRepository.save(savedShare);

        log.info("Created share session {} for user {}", savedShare.getId(), userId);

        SharedRecordDTO shareDTO = shareRecordMapper.toDTO(savedShare);
        shareDTO.setUserName(user.getName());
        shareDTO.setQrData(qrData);
        return shareDTO;
    }

    /**
     * Retrieves files associated with a QR code.
     *
     * @param qrCode The QR code string.
     * @return List of MedicalFileResponseDTO.
     * @throws QRCodeGenerationException if QR code is invalid.
     */
    @AuditLog(action = "GET_FILES_BY_QR_CODE")
    public List<MedicalFileResponseDTO> getFilesByQrCode(String qrCode) {
        SharedRecords sharedRecord = sharedRecordRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new QRCodeGenerationException("QR code غير صالح أو غير موجود"));

        return sharedRecord.getSharedFiles()
                .stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();
    }

    /**
     * Accesses a shared session using a QR code.
     * Validates expiration and status.
     *
     * @param qrCode The QR code string.
     * @return SharedRecordDTO containing session details.
     * @throws ResourceNotFoundException if session not found.
     * @throws BadRequestException if session is revoked or expired.
     */
    @AuditLog(action = "ACCESS_SHARE")
    public SharedRecordDTO accessShare(String qrCode) {
        SharedRecords share = sharedRecordRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("جلسة المشاركة غير موجودة"));

        if (share.getStatus() == SharedRecords.ShareStatus.REVOKED) {
            log.warn("Attempted to access revoked share session {}", share.getId());
            throw new BadRequestException("المستخدم أنهى جلسة المشاركة");
        }

        if (share.getExpiresAt().isBefore(LocalDateTime.now())) {
            share.setStatus(SharedRecords.ShareStatus.EXPIRED);
            sharedRecordRepository.save(share);
            log.warn("Attempted to access expired share session {}", share.getId());
            throw new BadRequestException("صلاحية المشاركة انتهت");
        }

        log.info("Accessed share session {}", share.getId());
        SharedRecordDTO dto = shareRecordMapper.toDTO(share);
        dto.setUserName(share.getUser().getName());
        String qrData = baseUrl + "/share/by-qr?qrCode=" + qrCode;
        dto.setQrData(qrData);
        return dto;
    }

    /**
     * Revokes an active sharing session.
     * Only the owner can revoke the session.
     *
     * @param shareId The ID of the session to revoke.
     * @param userDetails The authenticated user.
     * @return SharedRecordDTO with updated status.
     * @throws ResourceNotFoundException if session not found.
     * @throws BadRequestException if user is not owner or session already revoked.
     */
    @AuditLog(action = "REVOKE_SHARE")
    public SharedRecordDTO revokeShare(UUID shareId, UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        SharedRecords share = sharedRecordRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("جلسة المشاركة غير موجودة"));

        if (!share.getUser().getId().equals(userId)) {
            log.error("User {} attempted to revoke share session {} owned by {}", userId, shareId, share.getUser().getId());
            throw new BadRequestException("غير مسموح لك بإنهاء الجلسة");
        }

        if (share.getStatus() == SharedRecords.ShareStatus.REVOKED) {
            throw new BadRequestException("تم إنهاء الجلسة بالفعل");
        }

        share.setStatus(SharedRecords.ShareStatus.REVOKED);
        SharedRecords revokedShare = sharedRecordRepository.save(share);
        log.info("User {} revoked share session {}", userId, shareId);
        return shareRecordMapper.toDTO(revokedShare);
    }

    /**
     * Retrieves all sharing sessions for a user.
     * Categorizes sessions into active and expired.
     *
     * @param userDetails The authenticated user.
     * @return Map containing lists of active and expired sessions.
     * @throws ResourceNotFoundException if user not found.
     */
    @AuditLog(action = "GET_USER_SESSIONS")
    public Map<String, List<SharedRecordDTO>> getUserSessions(UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        List<SharedRecords> allSessions = sharedRecordRepository.findByUser(user);

        List<SharedRecordDTO> active = allSessions.stream()
                .filter(s -> s.getStatus() == SharedRecords.ShareStatus.ACTIVE)
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(shareRecordMapper::toDTO)
                .toList();

        List<SharedRecordDTO> expired = allSessions.stream()
                .filter(s -> s.getStatus() != SharedRecords.ShareStatus.ACTIVE ||
                        s.getExpiresAt().isBefore(LocalDateTime.now()))
                .map(shareRecordMapper::toDTO)
                .toList();

        log.debug("Retrieved {} active and {} expired sessions for user {}", active.size(), expired.size(), userId);
        return Map.of("active", active, "expired", expired);
    }

    /**
     * Validates that all files belong to the user.
     *
     * @param files List of files to check.
     * @param userId The user ID.
     * @throws BadRequestException if any file does not belong to the user.
     */
    private void validateFilesOwnership(List<MedicalFile> files, UUID userId) {
        boolean allBelong = files.stream()
                .allMatch(file -> file.getOwner().getId().equals(userId));

        if (!allBelong) {
            throw new BadRequestException("بعض الملفات لا تخص المستخدم الحالي");
        }
    }

    /**
     * Creates a new SharedRecords entity.
     *
     * @param user The user creating the share.
     * @param files The files being shared.
     * @param qrCode The generated QR code.
     * @return The new SharedRecords entity.
     */
    private SharedRecords createSharedRecord(User user, List<MedicalFile> files, String qrCode) {
        LocalDateTime now = LocalDateTime.now();

        SharedRecords sharedRecords = new SharedRecords();
        sharedRecords.setUser(user);
        sharedRecords.setSharedFiles(files);
        sharedRecords.setQrCode(qrCode);
        sharedRecords.setSharedAt(now);
        sharedRecords.setExpiresAt(now.plusMinutes(shareExpiryMinutes));
        sharedRecords.setStatus(SharedRecords.ShareStatus.ACTIVE);

        return sharedRecords;
    }
}
