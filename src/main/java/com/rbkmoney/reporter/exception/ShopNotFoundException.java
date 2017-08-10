package com.rbkmoney.reporter.exception;

public class ShopNotFoundException extends RuntimeException {

    public ShopNotFoundException(String message, Object... args) {
        this(String.format(message, args));
    }

    public ShopNotFoundException(String message, Throwable cause, Object... args) {
        this(String.format(message, args), cause);
    }

    public ShopNotFoundException(String message) {
        super(message);
    }

    public ShopNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShopNotFoundException(Throwable cause) {
        super(cause);
    }

    public ShopNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
