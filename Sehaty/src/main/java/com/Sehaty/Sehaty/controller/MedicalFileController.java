package com.Sehaty.Sehaty.controller;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-files")
@RequiredArgsConstructor
public class MedicalFileController {

    private final MedicalFileService medicalFileService;

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

    @GetMapping("/AllFiles")
    public ResponseEntity<ApiResponse> getAllFilesByUser(@AuthenticationPrincipal UserDetails userDetails) {

        List<MedicalFileResponseDTO> files = medicalFileService.getAllFilesByUser(userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملفات بنجاح", files)
        );
    }

    @GetMapping("/file")
    public ResponseEntity<ApiResponse> getFileById(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MedicalFileResponseDTO file = medicalFileService.getFileById(fileId, userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملف بنجاح", file)
        );
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<ApiResponse> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserDetails userDetails) {

        medicalFileService.deleteFile(fileId, userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم حذف الملف بنجاح")
        );
    }

    @GetMapping("/categories/{category}/subcategories")
    public ResponseEntity<ApiResponse> getSubcategoriesByCategory(@PathVariable String category) {
        List<String> subcategories;

        switch (category.toUpperCase()) {
            case "RADIOLOGY":
                subcategories = List.of("أشعة سينية", "أشعة مقطعية", "رنين مغناطيسي", "أشعة تليفزيونية");
                break;
            case "LABS":
                subcategories = List.of("تحليل دم", "تحليل بول", "تحليل سكر", "تحليل وظائف كبد");
                break;
            case "REPORTS":
                subcategories = List.of("روشتة", "تقرير طبي", "نتائج متابعة");
                break;
            default:
                subcategories = List.of("أخرى");
        }

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع أنواع الملفات الفرعية بنجاح", subcategories)
        );
    }

}
