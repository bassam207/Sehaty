package com.Sehaty.Sehaty.service;


import com.Sehaty.Sehaty.audit.AuditLog;
import com.Sehaty.Sehaty.exception.BadRequestException;
import com.Sehaty.Sehaty.exception.FileStorageException;
import com.Sehaty.Sehaty.exception.ResourceNotFoundException;
import com.Sehaty.Sehaty.exception.UnauthorizedException;
import com.Sehaty.Sehaty.shared.ApiResponse;
import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.shared.FileCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import java.util.UUID;

/**
 * Service for managing medical files.
 * Handles uploading, retrieving, and deleting files.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalFileService {

    private final MedicalFileRepository medicalFileRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final MedicalFileMapper medicalFileMapper;

    @Value("${sehaty.files.max-per-user}")
    private int maxFilesPerUser;

    @Value("#{'${sehaty.files.allowed-extensions}'.split(',')}")
    private List<String> allowedExtensions;

    /**
     * Upload medical file
     * @param userDetails User Details
     * @param file MultipartFile
     * @param requestDTO Upload request data
     * @return MedicalFileResponseDTO
     */
    @AuditLog(action = "UPLOAD_FILE")
    public MedicalFileResponseDTO uploadFile(UserDetails userDetails, MultipartFile file, MedicalFileUploadRequestDTO requestDTO) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long fileCount = medicalFileRepository.countByOwner(user);
        if (fileCount >= maxFilesPerUser) {
            throw new BadRequestException("Reached the maximum number of allowed files (" + maxFilesPerUser + " files)");
        }

        if (requestDTO.getCategory() == null || requestDTO.getCategory().isBlank()) {
            throw new BadRequestException("File category is required");
        }

        if (requestDTO.getSubCategory() == null || requestDTO.getSubCategory().isBlank()) {
            throw new BadRequestException("File subcategory is required (e.g., X-ray or Lab Test)");
        }

        if (requestDTO.getDisplayName() == null || requestDTO.getDisplayName().isBlank()) {
            throw new BadRequestException("Display name is required (e.g., Chest CT Scan)");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types: " +
                    String.join(", ", allowedExtensions));
        }

        FileCategory categoryEnum = FileCategory.fromArabic(requestDTO.getCategory())
                .orElseGet(() -> {
                    try {
                        return FileCategory.valueOf(requestDTO.getCategory().toUpperCase());
                    } catch (Exception e) {
                        throw new BadRequestException("Unknown category: " + requestDTO.getCategory());
                    }
                });

        String subKey = categoryEnum.resolveSubcategoryKey(requestDTO.getSubCategory());

        String fileUrl;
        try {
            fileUrl = fileUploadService.uploadFile(file);
        } catch (IOException e) {
            log.error("Failed to upload file for user {}", userId, e);
            throw new FileStorageException("Failed to upload file", e);
        }

        String uniqueFilename = generateUniqueFilename(originalFilename);

        MedicalFile medicalFile = new MedicalFile();
        medicalFile.setCategory(categoryEnum);
        medicalFile.setSubCategory(subKey);
        medicalFile.setFileName(uniqueFilename);
        medicalFile.setFileType(extension);
        medicalFile.setOwner(user);
        medicalFile.setUrl(fileUrl);
        medicalFile.setDisplayName(requestDTO.getDisplayName().trim());
        medicalFile.setUploadedAt(LocalDateTime.now());

        MedicalFile savedFile = medicalFileRepository.save(medicalFile);
        log.info("User {} uploaded file {} with category {}", userId, savedFile.getId(), categoryEnum);

        return medicalFileMapper.toMedicalFileResponseDTO(savedFile);
    }

    /**
     * Get all files by user
     * @param userDetails User Details
     * @return List of MedicalFileResponseDTO
     */
    @AuditLog(action = "GET_ALL_FILES")
    public List<MedicalFileResponseDTO> getAllFilesByUser(UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<MedicalFile> files = medicalFileRepository.findByOwnerId(userId);
        if (files.isEmpty()) {
            throw new ResourceNotFoundException("لا توجد ملفات خاصة بالمستخدم الحالي");
        }
        log.debug("Retrieved {} files for user {}", files.size(), userId);
        return files.stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();
    }

    /**
     * Delete file
     * @param fileId File ID
     * @param userDetails User Details (for authorization)
     */
    @AuditLog(action = "DELETE_FILE")
    public void deleteFile(UUID fileId, UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MedicalFile file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("الملف غير موجود"));

        if (!file.getOwner().getId().equals(userId)) {
            log.error("User {} attempted to delete file {} owned by {}", userId, fileId, file.getOwner().getId());
            throw new UnauthorizedException("غير مسموح لك حذف الملف");
        }

        medicalFileRepository.delete(file);
        log.info("User {} deleted file {}", userId, fileId);
    }

    /**
     * Get file by ID
     * @param fileId File ID
     * @param userDetails User Details (for authorization)
     * @return MedicalFileResponseDTO
     */
    @AuditLog(action = "GET_FILE_BY_ID")
    public MedicalFileResponseDTO getFileById(UUID fileId, UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MedicalFile file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("الملف غير موجود"));

        if (!file.getOwner().getId().equals(userId)) {
            log.error("User {} attempted to access file {} owned by {}", userId, fileId, file.getOwner().getId());
            throw new UnauthorizedException("غير مسموح لك الوصول لهذا الملف");
        }

        log.debug("User {} accessed file {}", userId, fileId);
        return medicalFileMapper.toMedicalFileResponseDTO(file);
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Generate unique filename
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String filenameWithoutExt = originalFilename.substring(0,
                originalFilename.lastIndexOf("."));

        filenameWithoutExt = filenameWithoutExt.replaceAll("[^a-zA-Z0-9-_]", "_");

        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        return filenameWithoutExt + "_" + timestamp + "_" + uniqueId + "." + extension;
    }
}
