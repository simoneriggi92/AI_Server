package com.gruppo3.ai.lab3.exception;

public class PositionsNotFoundException extends RuntimeException {
    public PositionsNotFoundException(Long minTime, Long maxTime) {
        super("could not find positions between mintime: '" + minTime + "'and maxTime: '" + maxTime + "'.");
    }
}
