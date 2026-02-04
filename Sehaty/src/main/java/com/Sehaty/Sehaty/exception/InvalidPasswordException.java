package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for invalid password (HTTP 400).
 * Thrown when a password does not meet the required criteria (e.g., length).
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
