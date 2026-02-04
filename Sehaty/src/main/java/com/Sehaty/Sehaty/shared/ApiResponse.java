package com.Sehaty.Sehaty.shared;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper.
 * Used to provide a consistent response structure across all API endpoints.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    /** Indicates if the request was successful. */
    private boolean success;

    /** A message describing the result of the request. */
    private String message;

    /** The data payload returned by the API (can be null). */
    private Object data;

    /**
     * Constructor for responses without data payload.
     *
     * @param success Indicates success or failure.
     * @param message Response message.
     */
    public ApiResponse(boolean success , String message)
    {
        this.success = success;
        this.message = message;
    }
}
