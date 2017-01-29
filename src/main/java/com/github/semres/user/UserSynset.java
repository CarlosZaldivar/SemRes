package com.github.semres.user;

import com.github.semres.Synset;

public class UserSynset extends Synset {
    public UserSynset(String representation) {
        super(representation);
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
