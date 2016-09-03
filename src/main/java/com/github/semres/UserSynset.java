package com.github.semres;

public class UserSynset extends Synset {
    public UserSynset(String id) {
        super(id);
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
