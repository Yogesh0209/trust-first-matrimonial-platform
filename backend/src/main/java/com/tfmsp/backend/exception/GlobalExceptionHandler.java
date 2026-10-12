package com.tfmsp.backend.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String resolveRequestId(WebRequest request) {
        if (request != null) {
            String header = request.getHeader("X-Request-Id");
            if (header != null && !header.isBlank()) {
                return header;
            }
        }
        return java.util.UUID.randomUUID().toString();
    }

    // 1. Catches @Valid errors (e.g. blank email, bad phone, short password)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<FieldErrorItem> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(new FieldErrorItem(fe.getField(), fe.getDefaultMessage()));
        }

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed",
                errors,
                resolveRequestId(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 2. Catches malformed JSON
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "MALFORMED_JSON",
                "Malformed request body or invalid value",
                null,
                resolveRequestId(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 3. Catches ResponseStatusException thrown by our business services
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        String code = ex.getStatusCode().toString();
        if (ex.getStatusCode() == HttpStatus.CONFLICT) {
            code = "CONFLICT";
        } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            code = ex.getReason() != null && ex.getReason().contains("PLAN")
                    ? "PLAN_RESTRICTED"
                    : "FORBIDDEN";
        } else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            code = "UNAUTHORIZED";
        } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            code = "NOT_FOUND";
        }

        ApiError body = new ApiError(ex.getStatusCode().value(), code, ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    // 4. Catches DB duplicate key conflicts
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                "This data conflicts with an existing record");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 5. Catch-all for unexpected 500 errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ApiError body = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
