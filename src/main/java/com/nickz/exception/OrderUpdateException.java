package com.nickz.exception;

public class OrderUpdateException extends RuntimeException {
    public OrderUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderUpdateException(String message) {
        super(message);
    }
}