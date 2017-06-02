package com.github.semres.gui;

public class IDAlreadyTakenException extends RuntimeException {
    public IDAlreadyTakenException() {
        super("ID already taken.");
    }
}
