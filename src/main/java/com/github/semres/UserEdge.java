package com.github.semres;

public class UserEdge extends Edge {
    public UserEdge(Synset pointedSynset, Synset originSynset, RelationType relationType, String description, double weight) {
        super(pointedSynset, originSynset, relationType, description, weight);
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
