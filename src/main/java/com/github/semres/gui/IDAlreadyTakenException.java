package com.github.semres.gui;

/**
 * Created by carlos on 4/20/17.
 */
public class IDAlreadyTakenException extends RuntimeException {
    public IDAlreadyTakenException() {
        super("ID already taken.");
    }
}
