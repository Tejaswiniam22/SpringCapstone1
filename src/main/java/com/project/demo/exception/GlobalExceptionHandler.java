package com.project.demo.exception;

import com.project.demo.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for the application.
 * Map application-specific exceptions to proper HTTP status codes and consistent ApiResponse body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 - validation errors from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> err = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            err.put(fe.getField(), fe.getDefaultMessage());
        }
        String message = "Validation failed: " + err.toString();
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    // 409 - duplicate resource (username taken)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 404 - resource not found
    @ExceptionHandler({ResourceNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    // 401 - auth related problems (invalid token, expired token, bad credentials)
    @ExceptionHandler({InvalidTokenException.class, RefreshTokenExpiredException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthProblems(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    // 500 - storage / IO problems
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageProblems(StorageException ex) {
        log.error("StorageException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Server storage error: " + ex.getMessage()));
    }

    // fallback - internal server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}
