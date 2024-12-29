package com.alten.producttrial.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "ACCESS_DENIED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    @ExceptionHandler({ResourceNotFoundException.class, UserAlreadyExistsException.class, UserNotFoundException.class, InvalidCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse errorResponse = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getStatus()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    private ErrorResponse buildErrorResponse(Exception ex) {
        if (ex instanceof CustomErrorException) {
            CustomErrorException customErrorException = (CustomErrorException) ex;
            return new ErrorResponse(customErrorException.getStatusCode().value(), customErrorException.getErrorKey(), customErrorException.getReason());
        }

        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "UNKNOWN_ERROR", "An unexpected error occurred.");
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class ErrorResponse {
        private int status;
        private String errorKey;
        private String message;
    }
}
