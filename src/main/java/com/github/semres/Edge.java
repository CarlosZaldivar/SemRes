package com.github.semres;


import org.apache.commons.lang.Validate;

public abstract class Edge {
    private final String pointedSynset;
    private final String originSynset;
    protected RelationType relationType;
    protected String description;
    protected double weight;

    protected Edge(String pointedSynset, String originSynset, RelationType relationType, double weight) {
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

    public Edge(String pointedSynset, String originSynset, String description, RelationType relationType, double weight) {
        this(pointedSynset, originSynset, relationType, weight);
        this.description = description;
    }

    protected Edge(Edge edge) {
        this.pointedSynset = edge.pointedSynset;
        this.originSynset = edge.originSynset;
        this.relationType = edge.relationType;
        this.description = edge.description;
        this.weight = edge.weight;
    }

    public String getPointedSynset() {
        return pointedSynset;
    }

    public double getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginSynset() {
        return originSynset;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public enum RelationType {HOLONYM, HYPERNYM, HYPONYM, MERONYM, OTHER}

    public String getId() {
        return originSynset + "-" + pointedSynset;
    }
}
