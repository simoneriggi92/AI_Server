package com.gruppo3.ai.lab3.exception;

public class ArchiveAlreadyOwnedException extends RuntimeException{
    public ArchiveAlreadyOwnedException() { super("All selected archives already bought");};
}
