package com.ticketmaster.bookingservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    record ErrorResponse(OffsetDateTime timestamp, int status,
            String error, String message, Map<String, String> fieldErrors) {

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(
                OffsetDateTime.now(), 400, "Validation Failed", "Request errors", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        OffsetDateTime.now(),
                        500,
                        "Internal Server Error",
                        ex.getMessage(), // <-- this sends actual error to Postman
                        null));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage());

        // If it's a seat availability error return 409
        if (ex.getMessage() != null && ex.getMessage().contains("Seats not available")) {
            return build(HttpStatus.CONFLICT, ex.getMessage());
        }
        if (ex.getMessage() != null && ex.getMessage().contains("Seat lock failed")) {
            return build(HttpStatus.CONFLICT, ex.getMessage());
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
