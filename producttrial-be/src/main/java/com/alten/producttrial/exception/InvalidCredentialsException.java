package com.alten.producttrial.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CustomErrorException {

    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "invalid_credentials", message);
    }
}
