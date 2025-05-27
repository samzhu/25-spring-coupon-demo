package com.example.demo.exception;

public class ExcessiveDiscountException extends RuntimeException {

    public ExcessiveDiscountException(String message) {
        super(message);
    }
}
