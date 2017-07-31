package com.github.semres;

public class RelationTypeAlreadyExistsException extends RuntimeException {
    public RelationTypeAlreadyExistsException() {
        super("Relation type already exits.");
    }
}
