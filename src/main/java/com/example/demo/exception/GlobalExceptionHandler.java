package com.example.demo.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TotalDiscountExceededException.class)
    public ResponseEntity<Map<String, String>> handleTotalDiscountExceededException(TotalDiscountExceededException ex) {
        log.warn("Handling TotalDiscountExceededException: {}", ex.getMessage());
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
}
