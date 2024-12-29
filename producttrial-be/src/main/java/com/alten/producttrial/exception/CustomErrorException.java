package com.alten.producttrial.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class CustomErrorException extends ErrorResponseException {

    private final String errorKey;
    private final String reason;

    public CustomErrorException(HttpStatus status, String errorKey, String reason) {
        this(status, errorKey, reason, null);
    }

    public CustomErrorException(HttpStatus status, String errorKey, String reason, Throwable cause) {
        super(status, ProblemDetail.forStatus(status), cause, null, null);
        this.errorKey = errorKey;
        this.reason = reason;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public String getReason() {
        return reason;
    }
}