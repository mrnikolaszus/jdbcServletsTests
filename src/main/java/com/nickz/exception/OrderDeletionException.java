package com.nickz.exception;

public class OrderDeletionException extends RuntimeException {
    public OrderDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}