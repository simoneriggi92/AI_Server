package com.gruppo3.ai.lab3.exception;

public class FailedPaymentException extends RuntimeException {
    public FailedPaymentException() {
        super("failed payment");
    }
}
