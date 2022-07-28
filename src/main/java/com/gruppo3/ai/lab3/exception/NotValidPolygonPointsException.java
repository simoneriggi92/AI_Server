package com.gruppo3.ai.lab3.exception;

public class NotValidPolygonPointsException extends RuntimeException {
    public NotValidPolygonPointsException() {
        super("polygon points are not valid");
    }
}
