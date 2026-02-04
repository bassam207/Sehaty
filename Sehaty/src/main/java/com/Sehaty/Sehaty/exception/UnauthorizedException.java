package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for unauthorized access (HTTP 403).
 * Thrown when a user attempts an action they are not permitted to perform.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
