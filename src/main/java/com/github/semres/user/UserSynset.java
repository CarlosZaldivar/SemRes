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

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
