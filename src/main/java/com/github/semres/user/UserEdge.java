package com.github.semres.user;

import com.github.semres.Edge;

public class UserEdge extends Edge {
    public UserEdge(String pointedSynset, String originSynset, RelationType relationType, double weight) {
        super(pointedSynset, originSynset, relationType, weight);
    }

    public UserEdge(String pointedSynset, String originSynset, String description, RelationType relationType, double weight) {
        super(pointedSynset, originSynset, description, relationType, weight);
    }

    public void setWeight(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException();
        }
        this.weight = weight;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
