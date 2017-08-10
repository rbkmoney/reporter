package com.rbkmoney.reporter.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String message, Object... args) {
        this(String.format(message, args));
    }

    public FileNotFoundException(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundException(Throwable cause) {
        super(cause);
    }

    public FileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
