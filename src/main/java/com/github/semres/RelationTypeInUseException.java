package com.github.semres;

public class RelationTypeInUseException extends RuntimeException {
    public RelationTypeInUseException() {
        super("Relation type in use, cannot be removed.");
    }
}
