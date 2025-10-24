package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.service.MedicalFileService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-files")
@RequiredArgsConstructor
public class MedicalFileController {

    private final MedicalFileService medicalFileService;

    @PostMapping("/upload/{userId}")
    public ResponseEntity<ApiResponse> uploadFile(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute MedicalFileUploadRequestDTO requestDTO) {

        MedicalFileResponseDTO responseDTO = medicalFileService.uploadFile(userId, file, requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم رفع الملف بنجاح", responseDTO));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getAllFilesByUser(@PathVariable UUID userId) {

        List<MedicalFileResponseDTO> files = medicalFileService.getAllFilesByUser(userId);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملفات بنجاح", files)
        );
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse> getFileById(
            @PathVariable UUID fileId,
            @RequestParam UUID userId) {

        MedicalFileResponseDTO file = medicalFileService.getFileById(fileId, userId);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملف بنجاح", file)
        );
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse> deleteFile(
            @PathVariable UUID fileId,
            @RequestParam UUID userId) {

        medicalFileService.deleteFile(fileId, userId);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم حذف الملف بنجاح")
        );
    }
}
