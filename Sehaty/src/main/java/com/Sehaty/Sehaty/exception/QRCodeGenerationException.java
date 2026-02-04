package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for QR code generation errors (HTTP 500).
 * Thrown when there is an issue with generating a QR code.
 */
public class QRCodeGenerationException extends RuntimeException {
    public QRCodeGenerationException(String message) {
        super(message);
    }

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
