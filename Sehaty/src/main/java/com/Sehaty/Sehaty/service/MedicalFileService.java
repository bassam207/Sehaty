package com.Sehaty.Sehaty.service;


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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalFileService {

    private final MedicalFileRepository medicalFileRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final MedicalFileMapper medicalFileMapper;

    private static final int MAX_FILES_PER_USER = 8;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "jpg", "jpeg", "png", "doc", "docx"
    );
    /**
     * Upload medical file
     * @param userId User ID
     * @param file MultipartFile
     * @param requestDTO Upload request data
     * @return MedicalFileResponseDTO
     */
    public MedicalFileResponseDTO uploadFile(UUID userId, MultipartFile file, MedicalFileUploadRequestDTO requestDTO) {

        // ✅ 1. التحقق من المستخدم
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        // ✅ 2. الحد الأقصى لعدد الملفات
        long fileCount = medicalFileRepository.countByOwner(user);
        if (fileCount >= MAX_FILES_PER_USER) {
            throw new BadRequestException("وصلت الحد الأقصى لعدد الملفات المسموح به (8 ملفات)");
        }

        // ✅ 3. التحقق من وجود الفئة
        if (requestDTO.getCategory() == null || requestDTO.getCategory().isBlank()) {
            throw new BadRequestException("يجب تحديد فئة الملف");
        }

        // ✅ 4. التحقق من وجود نوع فرعي
        if (requestDTO.getSubCategory() == null || requestDTO.getSubCategory().isBlank()) {
            throw new BadRequestException("يجب تحديد نوع الملف الفرعي (مثل نوع الأشعة أو التحليل)");
        }

        // ✅ 5. التحقق من الملف نفسه
        if (file.isEmpty()) {
            throw new BadRequestException("الملف فارغ");
        }

        // ✅ 6. التحقق من الامتداد
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("نوع الملف غير مسموح به. الأنواع المسموحة: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // ✅ 7. توليد اسم ملف فريد
        String uniqueFilename = generateUniqueFilename(originalFilename);

        // ✅ 8. رفع الملف إلى Cloudinary
        String fileUrl;
        try {
            fileUrl = fileUploadService.uploadFile(file);
        } catch (IOException e) {
            throw new FileStorageException("فشل رفع الملف", e);
        }

        // ✅ 9. تحويل الفئة من النص إلى Enum (يدعم العربي والإنجليزي)
        FileCategory categoryEnum = FileCategory.fromArabic(requestDTO.getCategory())
                .orElseGet(() -> {
                    try {
                        return FileCategory.valueOf(requestDTO.getCategory().toUpperCase());
                    } catch (Exception e) {
                        throw new BadRequestException("الفئة غير معروفة: " + requestDTO.getCategory());
                    }
                });

        // ✅ 10. إنشاء كائن الملف
        MedicalFile medicalFile = new MedicalFile();
        medicalFile.setCategory(categoryEnum);
        medicalFile.setFileName(uniqueFilename);
        medicalFile.setFileType(extension);
        medicalFile.setOwner(user);
        medicalFile.setUrl(fileUrl);
        medicalFile.setUploadedAt(LocalDateTime.now());

        // ✅ 11. تحديد النوع الفرعي بناءً على الفئة المختارة
        String subKey = categoryEnum.resolveSubcategoryKey(requestDTO.getSubCategory());
        medicalFile.setSubCategory(subKey);

        if (requestDTO.getDisplayName() == null || requestDTO.getDisplayName().isBlank()) {
            throw new BadRequestException("يجب إدخال اسم الملف (مثل أشعة مقطعية على الصدر)");
        }
        medicalFile.setDisplayName(requestDTO.getDisplayName().trim());

        // ✅ 12. حفظ الملف
        MedicalFile savedFile = medicalFileRepository.save(medicalFile);

        // ✅ 13. إرجاع النتيجة
        return medicalFileMapper.toMedicalFileResponseDTO(savedFile);
    }

    /**
     * Get all files by user
     * @param userId User ID
     * @return List of MedicalFileResponseDTO
     */
    public List<MedicalFileResponseDTO> getAllFilesByUser(UUID userId) {

        List<MedicalFile> files = medicalFileRepository.findByOwnerId(userId);

        if (files.isEmpty()) {
            throw new ResourceNotFoundException("لا توجد ملفات خاصة بالمستخدم الحالي");
        }

        return files.stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();
    }

    /**
     * Delete file
     * @param fileId File ID
     * @param userId User ID (for authorization)
     */
    public void deleteFile(UUID fileId, UUID userId) {

        MedicalFile file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("الملف غير موجود"));

        if (!file.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("غير مسموح لك حذف الملف");
        }

        medicalFileRepository.delete(file);
    }

    /**
     * Get file by ID
     * @param fileId File ID
     * @param userId User ID (for authorization)
     * @return MedicalFileResponseDTO
     */
    public MedicalFileResponseDTO getFileById(UUID fileId, UUID userId) {

        MedicalFile file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("الملف غير موجود"));

        if (!file.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("غير مسموح لك الوصول لهذا الملف");
        }

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

        // Clean filename (remove special characters)
        filenameWithoutExt = filenameWithoutExt.replaceAll("[^a-zA-Z0-9-_]", "_");

        // Add timestamp and UUID for uniqueness
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        return filenameWithoutExt + "_" + timestamp + "_" + uniqueId + "." + extension;
    }

}