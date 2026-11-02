package com.example.Booking.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            AlreadyExists ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "ALREADY_EXISTS",
                        Instant.now()
                ));

    }

    @ExceptionHandler(NotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            NotFound ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "NOT_FOUND",
                        Instant.now()
                ));
    }

    @ExceptionHandler(Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            Unauthorized ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "UNAUTHORIZED",
                        Instant.now()
                ));
    }

    @ExceptionHandler(Forbidden.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            Forbidden ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "FORBIDDEN",
                        Instant.now()
                ));
    }

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequest ex
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "BAD_REQUEST",
                        Instant.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgument(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation Failed");

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        message,
                        "VALIDATION_ERROR",
                        Instant.now()
                ));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthExceptions(
            RuntimeException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "INVALID_CREDENTIALS",
                        Instant.now()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "Invalid date format. Expected yyyy-MM-dd",
                        "INVALID_DATE_FORMAT",
                        Instant.now()
                ));
    }

    @ExceptionHandler(UnprocessableContent.class)
    public ResponseEntity<ErrorResponse> handle422(
            UnprocessableContent e
    ) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ErrorResponse(
                        e.getMessage(),
                        "UNPROCESSABLE_CONTENT",
                        Instant.now()
                ));
    }

    @ExceptionHandler(TooManyRequests.class)
    public ResponseEntity<ErrorResponse> handle429(TooManyRequests e) {
        ErrorResponse error = new ErrorResponse(
                e.getMessage(),
                "TOO_MANY_REQUESTS",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message, "BAD_REQUEST", Instant.now()));
    }


    @ExceptionHandler({
            TokenExpired.class,
            SessionExpired.class
    })
    public ResponseEntity<ErrorResponse> handleAuthExpiry(RuntimeException ex) {

        String code;

        if (ex instanceof TokenExpired) {
            code = "TOKEN_EXPIRED";
        } else {
            code = "SESSION_EXPIRED";
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        code,
                        Instant.now()
                ));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleGenerics(
            Exception ex
    ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "INTERNAL_ERROR",
                        Instant.now()
                ));
    }
}
