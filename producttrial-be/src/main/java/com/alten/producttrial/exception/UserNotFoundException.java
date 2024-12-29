package com.alten.producttrial.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomErrorException {
    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "user_not_found", message);
    }
}
