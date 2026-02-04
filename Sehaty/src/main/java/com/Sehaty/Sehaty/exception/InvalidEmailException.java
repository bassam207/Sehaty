package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for invalid email format (HTTP 400).
 * Thrown when an email address does not meet the required format.
 */
public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
