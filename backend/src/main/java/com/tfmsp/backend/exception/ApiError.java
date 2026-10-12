package com.tfmsp.backend.exception;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard error response envelope matching FRD Section 4.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

    private int status;
    private String code;
    private String message;
    private List<FieldErrorItem> errors;

    public ApiError() {
    }

    public ApiError(int status, String code, String message) {
        this(status, code, message, Collections.emptyList());
    }

    public ApiError(int status, String code, String message, List<FieldErrorItem> errors) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FieldErrorItem> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorItem> errors) {
        this.errors = errors;
    }
}
