package com.epita.common.utils;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import static jakarta.ws.rs.core.Response.status;

/**
 * Represents an HTTP error with an associated status and message.
 * This class provides methods to create and throw exceptions based on the error details.
 */
public class HttpError {

    /**
     * The HTTP status associated with the error.
     */
    public final Status status;

    /**
     * The error message template.
     */
    public final String message;

    /**
     * Constructs an {@code HttpError} with the specified HTTP status and message.
     *
     * @param status  the HTTP status associated with the error
     * @param message the error message template
     */
    public HttpError(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Creates a {@link RuntimeException} for the error with the specified arguments.
     *
     * @param args the arguments to format the error message
     * @return a {@link RuntimeException} containing the formatted error message
     */
    private RuntimeException createError(Object... args) {
        throw new WebApplicationException(
                status(this.status)
                        .entity(String.format(message, args))
                        .build()
        );
    }

    /**
     * Returns a {@link RuntimeException} for the error without additional arguments.
     *
     * @return a {@link RuntimeException} containing the error message
     */
    public RuntimeException get() {
        return createError();
    }

    /**
     * Returns a {@link RuntimeException} for the error with the specified arguments.
     *
     * @param args the arguments to format the error message
     * @return a {@link RuntimeException} containing the formatted error message
     */
    public RuntimeException get(Object... args) {
        return createError(args);
    }

    /**
     * Throws a {@link RuntimeException} for the error with the specified arguments.
     *
     * @param args the arguments to format the error message
     * @throws RuntimeException the exception containing the formatted error message
     */
    void throwException(Object... args) {
        throw get(args);
    }
}