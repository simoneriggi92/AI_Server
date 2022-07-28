package com.gruppo3.ai.lab3.exception;

public class PositionsChangedException extends RuntimeException{
    public PositionsChangedException() { super("Some positions are not the same of the previous request");};
}
