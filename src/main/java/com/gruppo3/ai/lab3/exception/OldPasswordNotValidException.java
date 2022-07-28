package com.gruppo3.ai.lab3.exception;

public class OldPasswordNotValidException extends RuntimeException {
    public OldPasswordNotValidException(){
        super("Old Password is not correct");
    }
}