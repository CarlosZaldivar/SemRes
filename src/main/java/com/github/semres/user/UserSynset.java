package com.github.semres.user;

import com.github.semres.Edge;
import com.github.semres.Synset;

import java.time.LocalDateTime;

public class UserSynset extends Synset {
    public UserSynset(String representation, String id) {
        super(representation, id);
    }

    public UserSynset(String representation, String id, String description) {
        super(representation, id, description);
    }

    private UserSynset(UserSynset copiedSynset) {
        super(copiedSynset);
    }

    public UserSynset(String representation, String id, String description, LocalDateTime lastEditedTime) {
        this(representation, id, description);
        this.lastEditedTime = lastEditedTime;
    }

    @Override
    public UserSynset addOutgoingEdge(Edge edge) {
        UserSynset newSynset = new UserSynset(this);
        newSynset.outgoingEdges.put(edge.getId(), edge);
        return newSynset;
    }

    @Override
    public UserSynset removeOutgoingEdge(String id) {
        UserSynset newSynset = new UserSynset(this);
        newSynset.outgoingEdges.remove(id);
        return newSynset;
    }

    public UserSynset changeRepresentation(String representation) {
        UserSynset newSynset = new UserSynset(this);
        newSynset.representation = representation;
        return newSynset;
    }

    public UserSynset changeDescription(String description) {
        UserSynset newSynset = new UserSynset(this);
        newSynset.description = description;
        return newSynset;
    }
}
