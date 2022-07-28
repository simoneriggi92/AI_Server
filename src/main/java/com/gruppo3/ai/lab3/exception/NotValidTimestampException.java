package com.gruppo3.ai.lab3.exception;

public class NotValidTimestampException extends RuntimeException {

    public NotValidTimestampException() {
        super("maxtime must be must be greater than or equal to mintime");
    }
}
