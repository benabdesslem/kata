package com.alten.producttrial.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CustomErrorException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "resource_not_found", message);
    }
}
