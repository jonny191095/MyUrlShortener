package com.example.MyURLShortener.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UrlNotFoundException extends Exception {
    public UrlNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
