package com.gruppo3.ai.lab3.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("Username already present!");
    }
}