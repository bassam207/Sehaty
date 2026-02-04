package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for file storage errors (HTTP 500).
 * Thrown when there is an issue with uploading or storing a file.
 */
public class FileStorageException extends RuntimeException{

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
