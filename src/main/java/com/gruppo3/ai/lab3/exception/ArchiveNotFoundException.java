package com.gruppo3.ai.lab3.exception;

public class ArchiveNotFoundException extends RuntimeException{
    public ArchiveNotFoundException(String archive_id) {
        super("could not find archive '" + archive_id + "'.");
    }
}
