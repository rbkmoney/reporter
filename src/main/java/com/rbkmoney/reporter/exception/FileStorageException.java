package com.rbkmoney.reporter.exception;

public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Object... args) {
        this(String.format(message, args));
    }

    public FileStorageException(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageException(Throwable cause) {
        super(cause);
    }

    public FileStorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
