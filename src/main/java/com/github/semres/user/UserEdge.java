package com.github.semres.user;

import com.github.semres.Edge;
import com.github.semres.Synset;

public class UserEdge extends Edge {
    public UserEdge(Synset toSynset, Synset originSynset, RelationType relationType, double weight) {
        super(toSynset, originSynset, relationType, weight);
    }

    public UserEdge(Synset pointedSynset, Synset originSynset, RelationType relationType, String description, double weight) {
        super(pointedSynset, originSynset, relationType, weight);
        this.description = description;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
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
