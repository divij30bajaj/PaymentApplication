package com.example.pomelo.exceptions;

import lombok.Data;

public class CustomException extends Exception {
    private String message;

    public CustomException(String message) {
        this.message = message;
    }
}
