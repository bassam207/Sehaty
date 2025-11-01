package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.service.FileShareService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class FileShareController {

    private final FileShareService fileShareService;

    /**
     * Create share session
     * POST /api/share/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createShareSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<UUID> fileIds) {

        SharedRecordDTO sharedRecord = fileShareService.createShareSession(userDetails, fileIds);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم إنشاء جلسة المشاركة بنجاح", sharedRecord));
    }

    @GetMapping("/by-qr")
    public ResponseEntity<ApiResponse> getFilesByQr(@RequestParam String qrCode) {
        List<MedicalFileResponseDTO> files = fileShareService.getFilesByQrCode(qrCode);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إرجاع الملفات المرتبطة بـ QR code بنجاح", files)
        );
    }

    /**
     * Access share session by QR code
     * GET /api/share/access/{qrCode}
     */
    @GetMapping("/access/{qrCode}")
    public ResponseEntity<ApiResponse> accessShare(@PathVariable String qrCode) {

        SharedRecordDTO sharedRecord = fileShareService.accessShare(qrCode);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم الوصول إلى جلسة المشاركة بنجاح", sharedRecord)
        );
    }

    /**
     * Revoke share session
     * PUT /api/share/{shareId}/revoke
     */
    @PutMapping("/revoke/{shareId}")
    public ResponseEntity<ApiResponse> revokeShare(
            @PathVariable UUID shareId,
           @AuthenticationPrincipal UserDetails userDetails) {

        SharedRecordDTO sharedRecord = fileShareService.revokeShare(shareId, userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إنهاء جلسة المشاركة بنجاح", sharedRecord)
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse> getUserSessions(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, List<SharedRecordDTO>> sessions =
                fileShareService.getUserSessions(userDetails);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم جلب الجلسات بنجاح", sessions)
        );
    }
}
