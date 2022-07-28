package com.gruppo3.ai.lab3.exception;

public class PassAndConfirmPassDontMatchException extends RuntimeException {

    public PassAndConfirmPassDontMatchException(){
        super("New Password and New Confirm Password don't match");
    }
}
