package com.rbkmoney.reporter.exception;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String message, Object... args) {
        this(String.format(message, args));
    }

    public ReportNotFoundException(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }

    public ReportNotFoundException(String message) {
        super(message);
    }

    public ReportNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportNotFoundException(Throwable cause) {
        super(cause);
    }

    public ReportNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
