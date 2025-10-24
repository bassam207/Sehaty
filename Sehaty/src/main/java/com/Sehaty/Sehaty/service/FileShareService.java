package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.exception.QRCodeGenerationException;
import com.Sehaty.Sehaty.exception.ResourceNotFoundException;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.mapper.ShareRecordMapper;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.SharedRecords;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.SharedRecordRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.shared.ApiResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.web.multipart.MultipartFile;
import com.Sehaty.Sehaty.exception.BadRequestException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class FileShareService {

    private final UserRepository userRepository;
    private final MedicalFileRepository medicalFileRepository;
    private final SharedRecordRepository sharedRecordRepository;
    private  final  FileUploadService fileUploadService;
    private final ShareRecordMapper shareRecordMapper;
    private final MedicalFileMapper medicalFileMapper;

    private static final int QR_CODE_SIZE = 250;
    private static final int SHARE_EXPIRY_HOURS = 1;

    /**
     * Create share session
     * @param userId User ID
     * @param fileIds List of file IDs to share
     * @return SharedRecordDTO
     */
    public SharedRecordDTO createShareSession(UUID userId, List<UUID> fileIds) {

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

        String qrUrl = generateAndUploadQRCode(qrCode, savedShare);

       SharedRecordDTO shareDTO = shareRecordMapper.toDTO(savedShare);
        shareDTO.setUserName(user.getName());
        shareDTO.setQrUrl(qrUrl);
      return   shareDTO;
    }

    public List<MedicalFileResponseDTO> getFilesByQrCode(String qrCode) {
        SharedRecords sharedRecord = sharedRecordRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new QRCodeGenerationException("QR code غير صالح أو غير موجود"));

        // هنا بترجع الملفات اللي كانت ضمن الجلسة دي
        return sharedRecord.getSharedFiles()
                .stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();
    }


    /**
     * Access share session by QR code
     * @param qrCode QR code string
     * @return SharedRecordDTO
     */
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

        return shareRecordMapper.toDTO(share);
    }

    /**
     * Revoke share session
     * @param shareId Share ID
     * @param userId User ID (for authorization)
     * @return SharedRecordDTO
     */
    public SharedRecordDTO revokeShare(UUID shareId, UUID userId) {

        SharedRecords share = sharedRecordRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("جلسة المشاركة غير موجودة"));

        if (!share.getUser().getId().equals(userId)) {
            throw new BadRequestException("غير مسموح لك بإنهاء الجلسة");
        }

        if (share.getStatus() == SharedRecords.ShareStatus.REVOKED) {
            throw new BadRequestException("تم إنهاء الجلسة بالفعل");
        }

        share.setStatus(SharedRecords.ShareStatus.REVOKED);
        SharedRecords revokedShare = sharedRecordRepository.save(share);

        return shareRecordMapper.toDTO(revokedShare);
    }

    // ==================== Helper Methods ====================

    private void validateFilesOwnership(List<MedicalFile> files, UUID userId) {
        boolean allBelongToUser = files.stream()
                .allMatch(file -> file.getOwner().getId().equals(userId));

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
        sharedRecords.setExpiresAt(now.plusHours(SHARE_EXPIRY_HOURS));
        sharedRecords.setStatus(SharedRecords.ShareStatus.ACTIVE);

        return sharedRecords;
    }

    private String generateAndUploadQRCode(String qrCode, SharedRecords savedShare) {
        try {

            String baseUrl = "http://localhost:8089/api/share/by-qr?qrCode=";
            String qrContent = baseUrl + qrCode;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] qrImageBytes = pngOutputStream.toByteArray();

            MultipartFile qrFile = new MockMultipartFile(
                    "file",
                    qrCode + ".png",
                    "image/png",
                    qrImageBytes
            );

            String qrUrl = fileUploadService.uploadFile(qrFile);

            savedShare.setQrUrl(qrUrl);
            sharedRecordRepository.save(savedShare);

            return qrUrl;

        } catch (Exception e) {
            throw new QRCodeGenerationException("فشل إنشاء رمز المشاركة");
        }
    }


    private SharedRecordDTO buildSharedRecordDTO(SharedRecords share, List<MedicalFile> files, String qrUrl) {
        SharedRecordDTO dto = new SharedRecordDTO();
        dto.setId(share.getId());
        dto.setQrCode(share.getQrCode());
        dto.setQrUrl(qrUrl);
        dto.setSharedAt(share.getSharedAt());
        dto.setExpiresAt(share.getExpiresAt());
        dto.setStatus(share.getStatus());
        dto.setSharedFiles(files.stream()
                .map(f -> new MedicalFileResponseDTO(
                        f.getId(),
                        f.getCategory(),
                        f.getSubCategory(),
                        f.getFileName(),
                        f.getUrl()
                ))
                .toList());

        return dto;
    }}

