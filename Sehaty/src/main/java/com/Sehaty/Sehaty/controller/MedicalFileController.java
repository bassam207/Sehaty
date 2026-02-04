package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.config.SubcategoryConfig;
import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.service.MedicalFileService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Controller for handling medical file management endpoints.
 * Includes uploading, retrieving, and deleting medical files.
 */
@RestController
@RequestMapping("/medical-files")
@RequiredArgsConstructor
public class MedicalFileController {

    private final MedicalFileService medicalFileService;
    private final SubcategoryConfig subcategoryConfig;

    /**
     * Uploads a new medical file.
     *
     * @param userDetails The authenticated user's details.
     * @param file The file to upload.
     * @param requestDTO DTO containing metadata for the file.
     * @return ApiResponse with details of the uploaded file.
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadFile(
           @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute MedicalFileUploadRequestDTO requestDTO) {

        MedicalFileResponseDTO responseDTO = medicalFileService.uploadFile(userDetails,file, requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم رفع الملف بنجاح", responseDTO));
    }

    /**
     * Retrieves all medical files for the authenticated user.
     *
     * @param userDetails The authenticated user's details.
     * @return ApiResponse containing a list of the user's medical files.
     */
    @GetMapping("/AllFiles")
    public ResponseEntity<ApiResponse> getAllFilesByUser(@AuthenticationPrincipal UserDetails userDetails) {

        List<MedicalFileResponseDTO> files = medicalFileService.getAllFilesByUser(userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملفات بنجاح", files)
        );
    }

    /**
     * Retrieves a specific medical file by its ID.
     *
     * @param fileId The ID of the file to retrieve.
     * @param userDetails The authenticated user's details.
     * @return ApiResponse containing the requested file's details.
     */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<ApiResponse> getFileById(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MedicalFileResponseDTO file = medicalFileService.getFileById(fileId, userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملف بنجاح", file)
        );
    }

    /**
     * Deletes a specific medical file by its ID.
     *
     * @param fileId The ID of the file to delete.
     * @param userDetails The authenticated user's details.
     * @return ApiResponse confirming successful deletion.
     */
    @DeleteMapping("/deleteFile/{fileId}")
    public ResponseEntity<ApiResponse> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserDetails userDetails) {

        medicalFileService.deleteFile(fileId, userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم حذف الملف بنجاح")
        );
    }

    /**
     * Retrieves subcategories for a given medical file category.
     *
     * @param category The main category (e.g., RADIOLOGY, LABS).
     * @return ApiResponse containing a list of subcategories.
     */
    @GetMapping("/categories/{category}/subcategories")
    public ResponseEntity<ApiResponse> getSubcategoriesByCategory(@PathVariable String category) {
        List<String> subcategories = subcategoryConfig.getSubcategories().getOrDefault(category.toUpperCase(), Collections.emptyList());
        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع أنواع الملفات الفرعية بنجاح", subcategories)
        );
    }

}
