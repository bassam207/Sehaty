package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.service.FileShareService;
import com.Sehaty.Sehaty.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @RequestParam UUID userId,
            @RequestBody List<UUID> fileIds) {

        SharedRecordDTO sharedRecord = fileShareService.createShareSession(userId, fileIds);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "تم إنشاء جلسة المشاركة بنجاح", sharedRecord));
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
    @PutMapping("/{shareId}/revoke")
    public ResponseEntity<ApiResponse> revokeShare(
            @PathVariable UUID shareId,
            @RequestParam UUID userId) {

        SharedRecordDTO sharedRecord = fileShareService.revokeShare(shareId, userId);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم إنهاء جلسة المشاركة بنجاح", sharedRecord)
        );
    }
}
