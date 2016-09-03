package com.github.semres;


abstract public class Edge {
    private final Synset pointedSynset;
    private final Synset originSynset;
    protected RelationType relationType;
    protected String description;
    protected double weight;

    public Edge(Synset toSynset, Synset originSynset, RelationType relationType, String description, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException();
        }
        this.pointedSynset = toSynset;
        this.originSynset = originSynset;
        this.relationType = relationType;
        this.description = description;
        this.weight = weight;
    }

    public Synset getPointedSynset() {
        return pointedSynset;
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

    public enum RelationType {HOLONYM, HYPERNY, HYPONYM, MERONYM, OTHER}

    public String getId() {
        return originSynset.getId() + "-" + pointedSynset.getId();
    }
}
