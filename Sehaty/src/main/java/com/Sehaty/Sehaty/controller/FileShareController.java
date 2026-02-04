package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.shared.ApiResponse;
import com.Sehaty.Sehaty.service.FileShareService;
import com.Sehaty.Sehaty.service.QrCodeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling file sharing endpoints.
 * Includes creating share sessions, generating QR codes, and accessing shared files.
 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class FileShareController {

    private final FileShareService fileShareService;
    private final QrCodeService qrCodeService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**

     *
     * @param userDetails - Authenticated user details.
     * @param fileIds - List of medical file IDs to share.
     * @return ApiResponse containing share session details including QR code URL and share URL.
     * @throws Exception if QR code generation fails.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createShareSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<UUID> fileIds) throws Exception {

        // Create share session in database
        SharedRecordDTO sharedRecord = fileShareService.createShareSession(userDetails, fileIds);

        // Build the complete share page URL (this is what QR code will contain)
        String shareUrl = frontendUrl + "/share/" + sharedRecord.getQrCode();

        // Build QR code image URL (for displaying the QR image in UI)
        String qrImageUrl = frontendUrl + "/api/share/qr/" + sharedRecord.getQrCode();

        // Set URLs in response DTO
        sharedRecord.setShareUrl(shareUrl);     // URL that opens when QR is scanned
        sharedRecord.setQrData(qrImageUrl);     // URL of QR code image for display

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Share session created successfully", sharedRecord));
    }

    /**
     * Generate and return QR code image as PNG.
     * The QR code contains the full share page URL.
     *
     * @param code - The UUID of the share session.
     * @param response - HTTP response to write PNG image bytes.
     * @throws Exception if QR code generation fails.
     */
    @GetMapping("/qr/{code}")
    public void getQrCodeImage(@PathVariable String code, HttpServletResponse response) throws Exception {
        // Build the complete share URL that will be embedded in QR code
        String shareUrl = frontendUrl + "/share/" + code;

        // Generate QR code as PNG bytes
        byte[] pngData = qrCodeService.generateQrCodePngBytes(shareUrl);

        // Set response headers and write image data
        response.setContentType("image/png");
        response.setContentLength(pngData.length);
        response.getOutputStream().write(pngData);
        response.getOutputStream().flush();
    }

    /**
     * Access shared files using the QR code (UUID).
     * This API is used by the frontend to retrieve shared data.
     *
     * @param code - The UUID from the QR code/share link.
     * @return ApiResponse containing shared record details and files.
     */
    @GetMapping("/access-by-qr")
    public ResponseEntity<ApiResponse> accessShareByQr(@RequestParam String code) {
        SharedRecordDTO sharedRecord = fileShareService.accessShare(code);
        return ResponseEntity.ok(
                new ApiResponse(true, "Successfully accessed share session", sharedRecord)
        );
    }

    /**
     * Revoke/cancel an active share session.
     * Only the owner can revoke their share session.
     *
     * @param shareId - ID of the share session to revoke.
     * @param userDetails - Authenticated user details (must be owner).
     * @return ApiResponse with updated share record status.
     */
    @PutMapping("/revoke/{shareId}")
    public ResponseEntity<ApiResponse> revokeShare(
            @PathVariable UUID shareId,
            @AuthenticationPrincipal UserDetails userDetails) {
        SharedRecordDTO sharedRecord = fileShareService.revokeShare(shareId, userDetails);
        return ResponseEntity.ok(
                new ApiResponse(true, "Share session revoked successfully", sharedRecord)
        );
    }

    /**
     * Get all share sessions for the authenticated user.
     * Returns both active and expired sessions.
     *
     * @param userDetails - Authenticated user details.
     * @return ApiResponse containing categorized share sessions (active/expired).
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse> getUserSessions(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, List<SharedRecordDTO>> sessions = fileShareService.getUserSessions(userDetails);
        return ResponseEntity.ok(
                new ApiResponse(true, "User share sessions retrieved successfully", sessions)
        );
    }

    /**
     * Optional: Get QR code as Base64 string.
     * Useful for embedding directly in HTML/JSON responses.
     *
     * @param code - The UUID of the share session.
     * @return ApiResponse containing Base64-encoded QR code image.
     * @throws Exception if QR code generation fails.
     */
    @GetMapping("/qr/{code}/base64")
    public ResponseEntity<ApiResponse> getQrCodeBase64(@PathVariable String code) throws Exception {
        // Build the complete share URL
        String shareUrl = frontendUrl + "/share/" + code;

        // Generate QR code as Base64 string
        String base64Qr = qrCodeService.generateQrCodeBase64(shareUrl);

        return ResponseEntity.ok(
                new ApiResponse(true, "QR code generated successfully",
                        Map.of("qrCodeBase64", base64Qr, "shareUrl", shareUrl))
        );
    }
}
