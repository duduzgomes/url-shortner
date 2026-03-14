package com.eduardogomes.shortner_url.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eduardogomes.shortner_url.controllers.dto.ErrorResponse;
import com.eduardogomes.shortner_url.models.exceptions.UrlNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
   @ExceptionHandler(UrlNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(UrlNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, "URL não encontrada", ex.getMessage()));
}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
        MethodArgumentNotValidException ex) {

    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .findFirst()
        .orElse("Requisição inválida");

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of(
            "status", 400,
            "error", "Validação falhou",
            "message", message,
            "timestamp", Instant.now().toString()
        ));
    }
}
