package com.epita.common.utils;

import static jakarta.ws.rs.core.Response.Status;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

/**
 * Enum representing various error codes and their associated HTTP status and messages.
 * Each error code is mapped to a specific {@link HttpError} instance, which contains
 * the HTTP status and a formatted error message.
 */
public enum ErrorCode {
    POST_NOT_FOUND(NOT_FOUND, "Post %s does not exist."),
    USER_NOT_FOUND(NOT_FOUND, "User %s does not exist."),
    FORBIDDEN_ACTION(FORBIDDEN, "User %s is not authorized to perform this action."),
    UNEXPECTED_ERROR(INTERNAL_SERVER_ERROR, "An unexpected error occurred."),
    POST_INVALID_REQUEST(BAD_REQUEST, "Post %s is invalid."),
    POST_CREATION_FORBIDDEN(FORBIDDEN, "%s Cannot create post because %s is blocked / blocked sender"),
    REPO_SOCIAL_FAILED(SERVICE_UNAVAILABLE, "Rest request to RepoSocial failed: %s"),
    FILE_STORAGE_FAILED(INTERNAL_SERVER_ERROR, "File storage failed: %s"),
    FILE_RETRIEVAL_FAILED(INTERNAL_SERVER_ERROR, "File retrieval failed: %s"),
    FILE_DELETION_FAILED(INTERNAL_SERVER_ERROR, "File deletion failed: %s"),
    LIKE_NOT_FOUNT(NOT_FOUND, "Like not found."),
    HOME_TIMELINE_NOT_FOUND(NOT_FOUND, "Cannot find home timeline for user with it '%d'"),;

    /**
     * The {@link HttpError} instance associated with the error code.
     */
    private final HttpError error;

    /**
     * Constructs an {@code ErrorCode} with the specified HTTP status and message.
     *
     * @param status  the HTTP status associated with the error
     * @param message the error message template
     */
    ErrorCode(Status status, String message) {
        error = new HttpError(status, message);
    }

    /**
     * Returns a {@link RuntimeException} for the error with the specified arguments.
     *
     * @param args the arguments to format the error message
     * @return a {@link RuntimeException} containing the formatted error message
     */
    public RuntimeException get(Object... args) {
        return error.get(args);
    }

    /**
     * Throws a {@link RuntimeException} for the error with the specified arguments.
     *
     * @param args the arguments to format the error message
     */
    public void throwException(Object... args) {
        error.throwException(args);
    }
}
