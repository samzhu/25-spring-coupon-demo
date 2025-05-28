package com.example.demo.exception;

public class TotalDiscountExceededException extends RuntimeException {

    public TotalDiscountExceededException(String message) {
        super(message);
    }
}
