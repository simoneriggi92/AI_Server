package com.gruppo3.ai.lab3.exception;

public class NotEnoughMoneyException extends RuntimeException {
    public NotEnoughMoneyException() {
        super("your credit is insufficient");
    }
}