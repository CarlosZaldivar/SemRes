package com.github.semres;

public class RelationTypeAlreadyExists extends RuntimeException{
    public RelationTypeAlreadyExists() {
        super("Relation type already exits.");
    }
}
