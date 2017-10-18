package com.rbkmoney.reporter.exception;

public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Object... args) {
        super(String.format(message, args));
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(String message, Throwable cause, Object... args) {
        super(String.format(message, args), cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
