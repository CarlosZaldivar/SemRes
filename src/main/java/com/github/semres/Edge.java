package com.github.semres;


import org.apache.commons.lang.Validate;

public abstract class Edge {
    private Synset pointedSynset;
    private final Synset originSynset;
    protected RelationType relationType;
    protected String description;
    protected double weight;

    public Edge(Synset pointedSynset, Synset originSynset, RelationType relationType, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException();
        }

        Validate.notNull(pointedSynset, "Pointed synset cannot be null");
        Validate.notNull(originSynset, "Origin synset cannot be null");
        Validate.notNull(relationType, "Relation type cannot be null");

        this.pointedSynset = pointedSynset;
        this.originSynset = originSynset;
        this.relationType = relationType;
        this.weight = weight;
    }

    public Synset getPointedSynset() {
        return pointedSynset;
    }


    public void setPointedSynset(Synset pointedSynset) {
        this.pointedSynset = pointedSynset;
    }

    public double getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public Synset getOriginSynset() {
        return originSynset;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public enum RelationType {HOLONYM, HYPERNYM, HYPONYM, MERONYM, OTHER}

    public String getId() {
        return originSynset.getId() + "-" + pointedSynset.getId();
    }
}
