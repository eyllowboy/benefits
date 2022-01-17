package com.andersenlab.benefits.exception;

public class DiscountNotFoundException extends RuntimeException {

    public DiscountNotFoundException(String message) {
        super(message);
    }
}
