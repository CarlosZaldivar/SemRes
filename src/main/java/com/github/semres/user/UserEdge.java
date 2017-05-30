package com.github.semres.user;

import com.github.semres.Edge;
import com.github.semres.RelationType;

public class UserEdge extends Edge {
    public UserEdge(String pointedSynset, String originSynset, RelationType relationType, double weight) {
        super(pointedSynset, originSynset, relationType, weight);
    }

    public UserEdge(String pointedSynset, String originSynset, String description, RelationType relationType, double weight) {
        super(pointedSynset, originSynset, description, relationType, weight);
    }

    public UserEdge(Edge edge) {
        super(edge);
    }

    public UserEdge changeWeight(double weight) {
        if (weight < 0 || weight > 1) {
            throw new IllegalArgumentException();
        }
        UserEdge newEdge = new UserEdge(this);
        newEdge.weight = weight;
        return newEdge;
    }

    public UserEdge changeDescription(String description) {
        UserEdge newEdge = new UserEdge(this);
        newEdge.description = description;
        return newEdge;
    }

    public UserEdge changeRelationType(RelationType relationType) {
        UserEdge newEdge = new UserEdge(this);
        newEdge.relationType = relationType;
        return newEdge;
    }
}
