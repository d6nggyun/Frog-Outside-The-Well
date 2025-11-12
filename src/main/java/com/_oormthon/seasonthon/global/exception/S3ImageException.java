package com._oormthon.seasonthon.global.exception;

public class S3ImageException extends RuntimeException {
    public S3ImageException() {

    }

    public S3ImageException(String message) {
        super(message);
    }

    public S3ImageException(String message, Throwable cause) {
        super(message, cause);
    }

    public S3ImageException(Throwable cause) {
        super(cause);
    }

    public S3ImageException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}