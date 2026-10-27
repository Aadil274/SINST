package com.sinst.exception;

/**
 * Thrown when an authenticated student attempts to view, modify, or delete
 * records owned by another student (horizontal privilege escalation / IDOR prevention),
 * or when an unauthenticated request attempts to execute student-scoped operations.
 */
public class UnauthorizedAccessException extends SinstException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
