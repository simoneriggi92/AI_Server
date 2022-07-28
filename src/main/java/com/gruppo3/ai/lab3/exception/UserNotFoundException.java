package com.gruppo3.ai.lab3.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("could not find user '" + username + "'.");
    }
}
