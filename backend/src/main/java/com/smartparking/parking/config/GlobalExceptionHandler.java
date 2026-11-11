package com.smartparking.parking.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Invalid request body"
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("details", details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Invalid email or password"
        ));
    }

    @ExceptionHandler(com.smartparking.parking.exception.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(com.smartparking.parking.exception.ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Resource not found"
        ));
    }

    @ExceptionHandler(com.smartparking.parking.exception.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleCustomAccessDenied(com.smartparking.parking.exception.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Access denied"
        ));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleSpringAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Access denied"
        ));
    }

    @ExceptionHandler(com.smartparking.parking.exception.InvalidParkingSpaceException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParkingSpace(com.smartparking.parking.exception.InvalidParkingSpaceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Bad request"
        ));
    }

    @ExceptionHandler(com.smartparking.parking.exception.DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResource(com.smartparking.parking.exception.DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Duplicate resource"
        ));
    }

    @ExceptionHandler(com.smartparking.parking.exception.InvalidVehicleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidVehicle(com.smartparking.parking.exception.InvalidVehicleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Bad request"
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Bad request"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Bad request"
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        if ("Email already registered".equals(ex.getMessage())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "An internal error occurred"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "An internal error occurred"
        ));
    }
}
