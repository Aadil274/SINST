package com.sinst.exception;

public class SinstException extends Exception {
    public SinstException(String message) {
        super(message);
    }

    public SinstException(String message, Throwable cause) {
        super(message, cause);
    }
}
