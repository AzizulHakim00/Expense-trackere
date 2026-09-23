package com.example.expensetracker.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages="com.example.expensetracker.controller")
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalid(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage(),"timestamp",Instant.now().toString()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Invalid expense data",
            "fields",ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField()).distinct().toList()));
    }
}
