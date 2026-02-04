package com.Sehaty.Sehaty.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Service for generating QR codes.
 * Supports generating QR codes as PNG byte arrays or Base64 strings.
 */
@Slf4j
@Service
public class QrCodeService {

    /**
     * Generate QR Code as PNG byte array
     *
     * @param text - Content to encode in QR code (typically a URL)
     * @param width - QR code width in pixels
     * @param height - QR code height in pixels
     * @return byte array representing PNG image
     * @throws Exception if QR code generation fails
     */
    public byte[] generateQrCodePngBytes(String text, int width, int height) throws Exception {
        // Validate input text
        if (text == null || text.trim().isEmpty()) {
            log.error("Cannot generate QR code: text is null or empty");
            throw new IllegalArgumentException("QR code text cannot be null or empty");
        }

        // Validate dimensions
        if (width <= 0 || height <= 0) {
            log.error("Invalid QR code dimensions: width={}, height={}", width, height);
            throw new IllegalArgumentException("QR code dimensions must be positive");
        }

        try {
            log.debug("Generating QR code for: {}", text);

            // Create QR code writer (optimized for QR codes specifically)
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Encode text into QR code matrix
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            // Convert matrix to PNG byte array
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            byte[] pngData = pngOutputStream.toByteArray();
            log.debug("QR code generated successfully, size: {} bytes", pngData.length);

            return pngData;

        } catch (WriterException e) {
            log.error("Failed to encode QR code: {}", e.getMessage(), e);
            throw new Exception("Failed to generate QR code: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to write QR code to stream: {}", e.getMessage(), e);
            throw new Exception("Failed to save QR code image: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QR Code as Base64 string with data URI prefix
     * Useful for embedding directly in HTML <img> tags
     *
     * @param text - Content to encode in QR code
     * @param width - QR code width in pixels
     * @param height - QR code height in pixels
     * @return Base64 string with "data:image/png;base64," prefix
     * @throws Exception if QR code generation fails
     */
    public String generateQrCodeBase64(String text, int width, int height) throws Exception {
        // Generate PNG bytes
        byte[] pngData = generateQrCodePngBytes(text, width, height);

        // Encode to Base64
        String base64 = Base64.getEncoder().encodeToString(pngData);
        log.debug("QR code converted to Base64, length: {} characters", base64.length());

        // Return with data URI prefix for direct HTML embedding
        return "data:image/png;base64," + base64;
    }

    /**
     * Generate QR Code with default size (300x300 pixels)
     *
     * @param text - Content to encode in QR code
     * @return byte array representing PNG image
     * @throws Exception if QR code generation fails
     */
    public byte[] generateQrCodePngBytes(String text) throws Exception {
        return generateQrCodePngBytes(text, 300, 300);
    }

    /**
     * Generate QR Code as Base64 with default size (300x300 pixels)
     *
     * @param text - Content to encode in QR code
     * @return Base64 string with data URI prefix
     * @throws Exception if QR code generation fails
     */
    public String generateQrCodeBase64(String text) throws Exception {
        return generateQrCodeBase64(text, 300, 300);
    }
}
