package com.Sehaty.Sehaty.exception;

/**
 * Custom exception for internal server errors (HTTP 500).
 * Thrown for unexpected errors that occur on the server.
 */
public class InternalServerErrorException extends RuntimeException{

    public InternalServerErrorException(String message) {
        super(message);
    }
}
