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
import org.springframework.security.core.userdetails.UserDetails;
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

    private static final int MAX_FILES_PER_USER = 10;
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
    public MedicalFileResponseDTO uploadFile(UserDetails userDetails, MultipartFile file, MedicalFileUploadRequestDTO requestDTO) {

        // ✅ 1. استخرج البريد من التوكن
        String email = userDetails.getUsername();

        // ✅ 2. التحقق من المستخدم
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        // ✅ 2. التحقق من الحد الأقصى
        long fileCount = medicalFileRepository.countByOwner(user);
        if (fileCount >= MAX_FILES_PER_USER) {
            throw new BadRequestException("وصلت الحد الأقصى لعدد الملفات المسموح به (8 ملفات)");
        }

        // ✅ 3. التحقق من الفئة
        if (requestDTO.getCategory() == null || requestDTO.getCategory().isBlank()) {
            throw new BadRequestException("يجب تحديد فئة الملف");
        }

        // ✅ 4. التحقق من النوع الفرعي
        if (requestDTO.getSubCategory() == null || requestDTO.getSubCategory().isBlank()) {
            throw new BadRequestException("يجب تحديد نوع الملف الفرعي (مثل نوع الأشعة أو التحليل)");
        }

        // ✅ 5. التحقق من اسم العرض
        if (requestDTO.getDisplayName() == null || requestDTO.getDisplayName().isBlank()) {
            throw new BadRequestException("يجب إدخال اسم الملف (مثل أشعة مقطعية على الصدر)");
        }

        // ✅ 6. التحقق من الملف نفسه
        if (file.isEmpty()) {
            throw new BadRequestException("الملف فارغ");
        }

        // ✅ 7. التحقق من الامتداد
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("نوع الملف غير مسموح به. الأنواع المسموحة: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // ✅ 8. التحقق من الفئة وتحويلها إلى Enum (قبل رفع الملف)
        FileCategory categoryEnum = FileCategory.fromArabic(requestDTO.getCategory())
                .orElseGet(() -> {
                    try {
                        return FileCategory.valueOf(requestDTO.getCategory().toUpperCase());
                    } catch (Exception e) {
                        throw new BadRequestException("الفئة غير معروفة: " + requestDTO.getCategory());
                    }
                });

        // ✅ 9. التحقق من النوع الفرعي المتوافق مع الفئة
        String subKey = categoryEnum.resolveSubcategoryKey(requestDTO.getSubCategory());

        // ✅ 10. بعد ما نتأكد من كل حاجة، نرفع الملف
        String fileUrl;
        try {
            fileUrl = fileUploadService.uploadFile(file);
        } catch (IOException e) {
            throw new FileStorageException("فشل رفع الملف", e);
        }

        // ✅ 11. إنشاء كائن الملف
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

        // ✅ 12. حفظ الملف في قاعدة البيانات
        MedicalFile savedFile = medicalFileRepository.save(medicalFile);

        // ✅ 13. إرجاع النتيجة
        return medicalFileMapper.toMedicalFileResponseDTO(savedFile);
    }

    /**
     * Get all files by user
     * @param userId User ID
     * @return List of MedicalFileResponseDTO
     */
    public List<MedicalFileResponseDTO> getAllFilesByUser(UserDetails userDetails) {

        String email = userDetails.getUsername();
        List<MedicalFile> files = medicalFileRepository.findByOwnerEmail(email);

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
    public void deleteFile(UUID fileId, UserDetails userDetails) {

        String email = userDetails.getUsername();
        MedicalFile file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("الملف غير موجود"));

        if (!file.getOwner().getEmail().equalsIgnoreCase(email)) {
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
    public MedicalFileResponseDTO getFileById(UUID fileId, UserDetails userDetails) {

        String email = userDetails.getUsername();
        MedicalFile file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("الملف غير موجود"));

        if (!file.getOwner().getEmail().equalsIgnoreCase(email)) {
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