package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for email already in use (HTTP 409).
 * Thrown during registration if the email address is already associated with an account.
 */
public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
