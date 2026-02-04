package com.Sehaty.Sehaty.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure.
 * Returned when an exception occurs during API processing.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    /** HTTP status code of the error. */
    private int status;

    /** Descriptive error message. */
    private String message;

    /** Timestamp when the error occurred. */
    private LocalDateTime timestamp;
}
