package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for bad requests (HTTP 400).
 * Thrown when client input is invalid or malformed.
 */
public class BadRequestException extends RuntimeException{

    public BadRequestException(String message) {
        super(message);
    }
}
