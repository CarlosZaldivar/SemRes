package com.github.semres.user;

import com.github.semres.Edge;
import com.github.semres.Synset;

public class UserSynset extends Synset {
    public UserSynset(String representation) {
        super(representation);
    }
    private UserSynset(UserSynset copiedSynset) {
        super(copiedSynset);
    }

    @Override
    protected UserSynset addOutgoingEdge(Edge edge) {
        UserSynset newSynset = new UserSynset(this);
        newSynset.outgoingEdges.put(edge.getId(), edge);
        return newSynset;
    }

    @Override
    protected UserSynset removeOutgoingEdge(String id) {
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
