package com.gruppo3.ai.lab3.exception;

import com.gruppo3.ai.lab3.model.InvalidPositionEntity;

import java.util.LinkedList;

public class PersonalAccessException extends RuntimeException {

    public PersonalAccessException() {
        super("Access denied!");
    }
}
