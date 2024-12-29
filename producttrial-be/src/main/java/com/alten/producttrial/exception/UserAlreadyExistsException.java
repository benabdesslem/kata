package com.alten.producttrial.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends CustomErrorException {
    public UserAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, "user_already_exists", message);
    }
}
