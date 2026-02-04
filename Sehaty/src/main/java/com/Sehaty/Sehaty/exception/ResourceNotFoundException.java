package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for resource not found (HTTP 404).
 * Thrown when a requested resource (e.g., user, file) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
