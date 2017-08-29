package com.rbkmoney.reporter.exception;

public class SignException extends RuntimeException {

    public SignException(String message, Object... args) {
        this(String.format(message, args));
    }

    public SignException(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }

    public SignException(String message) {
        super(message);
    }

    public SignException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignException(Throwable cause) {
        super(cause);
    }

    public SignException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
