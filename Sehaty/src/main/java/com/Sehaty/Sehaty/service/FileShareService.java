package com.Sehaty.Sehaty.service;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileShareService {

    private final UserRepository userRepository;
    private final MedicalFileRepository medicalFileRepository;
    private final SharedRecordRepository sharedRecordRepository;
    private final FileUploadService fileUploadService;
    private final ShareRecordMapper shareRecordMapper;
    private final MedicalFileMapper medicalFileMapper;

    private static final int SHARE_EXPIRY_Minutes = 15;
    private static final String BASE_URL = "http://localhost:8089";

    public SharedRecordDTO createShareSession(UserDetails userDetails, List<UUID> fileIds) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        List<MedicalFile> files = medicalFileRepository.findAllById(fileIds);
        if (files.isEmpty()) {
            throw new ResourceNotFoundException("لا توجد ملفات للمشاركة");
        }

        validateFilesOwnership(files, email);

        String qrCode = UUID.randomUUID().toString();
        SharedRecords sharedRecords = createSharedRecord(user, files, qrCode);
        SharedRecords savedShare = sharedRecordRepository.save(sharedRecords);

        String qrData = BASE_URL + "/share/by-qr?qrCode=" + qrCode;
        sharedRecords.setQrData(qrData);
        savedShare = sharedRecordRepository.save(savedShare);

        SharedRecordDTO shareDTO = shareRecordMapper.toDTO(savedShare);
        shareDTO.setUserName(user.getName());
        shareDTO.setQrData(qrData);
        return shareDTO;
    }

    public List<MedicalFileResponseDTO> getFilesByQrCode(String qrCode) {
        SharedRecords sharedRecord = sharedRecordRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new QRCodeGenerationException("QR code غير صالح أو غير موجود"));

        return sharedRecord.getSharedFiles()
                .stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();
    }

    public SharedRecordDTO accessShare(String qrCode) {
        SharedRecords share = sharedRecordRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("جلسة المشاركة غير موجودة"));

        if (share.getStatus() == SharedRecords.ShareStatus.REVOKED) {
            throw new BadRequestException("المستخدم أنهى جلسة المشاركة");
        }

        if (share.getExpiresAt().isBefore(LocalDateTime.now())) {
            share.setStatus(SharedRecords.ShareStatus.EXPIRED);
            sharedRecordRepository.save(share);
            throw new BadRequestException("صلاحية المشاركة انتهت");
        }

        SharedRecordDTO dto = shareRecordMapper.toDTO(share);
        dto.setUserName(share.getUser().getName());
        String qrData = BASE_URL + "/share/by-qr?qrCode=" + qrCode;
        dto.setQrData(qrData);
        return dto;
    }

    public SharedRecordDTO revokeShare(UUID shareId, UserDetails userDetails) {
        String email = userDetails.getUsername();
        SharedRecords share = sharedRecordRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("جلسة المشاركة غير موجودة"));

        if (!share.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new BadRequestException("غير مسموح لك بإنهاء الجلسة");
        }

        if (share.getStatus() == SharedRecords.ShareStatus.REVOKED) {
            throw new BadRequestException("تم إنهاء الجلسة بالفعل");
        }

        share.setStatus(SharedRecords.ShareStatus.REVOKED);
        SharedRecords revokedShare = sharedRecordRepository.save(share);
        return shareRecordMapper.toDTO(revokedShare);
    }

    public Map<String, List<SharedRecordDTO>> getUserSessions(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
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

        return Map.of("active", active, "expired", expired);
    }

    private void validateFilesOwnership(List<MedicalFile> files, String email) {
        boolean allBelongToUser = files.stream()
                .allMatch(file -> file.getOwner().getEmail().equalsIgnoreCase(email));

        if (!allBelongToUser) {
            throw new BadRequestException("بعض الملفات لا تخص المستخدم الحالي");
        }
    }

    private SharedRecords createSharedRecord(User user, List<MedicalFile> files, String qrCode) {
        LocalDateTime now = LocalDateTime.now();

        SharedRecords sharedRecords = new SharedRecords();
        sharedRecords.setUser(user);
        sharedRecords.setSharedFiles(files);
        sharedRecords.setQrCode(qrCode);
        sharedRecords.setSharedAt(now);
        sharedRecords.setExpiresAt(now.plusMinutes(SHARE_EXPIRY_Minutes));
        sharedRecords.setStatus(SharedRecords.ShareStatus.ACTIVE);

        return sharedRecords;
    }
}