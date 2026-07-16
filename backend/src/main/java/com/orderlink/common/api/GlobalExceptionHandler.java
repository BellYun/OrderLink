package com.orderlink.common.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orderlink.product.application.DuplicateSkuException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"),
                (first, ignored) -> first,
                LinkedHashMap::new
            ));

        return createResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            request,
            fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return createResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "Request body is missing or malformed",
            request,
            Map.of()
        );
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSku(
        DuplicateSkuException exception,
        HttpServletRequest request
    ) {
        return createResponse(
            HttpStatus.CONFLICT,
            "DUPLICATE_SKU",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidDomainState(
        RuntimeException exception,
        HttpServletRequest request
    ) {
        return createResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> createResponse(
        HttpStatus status,
        String code,
        String message,
        HttpServletRequest request,
        Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            code,
            message,
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.status(status).body(response);
    }
}
