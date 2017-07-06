package com.github.semres;


import org.apache.commons.lang.Validate;

import java.time.LocalDateTime;

public abstract class Edge {
    private final String pointedSynset;
    private final String originSynset;
    protected RelationType relationType;
    protected String description;
    protected double weight;
    private LocalDateTime lastEditedTime;

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

    public Edge(String pointedSynset, String originSynset, String description, RelationType relationType, double weight, LocalDateTime lastEditedTime) {
        this(pointedSynset, originSynset, description, relationType, weight);
        this.lastEditedTime = lastEditedTime;
    }

    public LocalDateTime getLastEditedTime() {
        return lastEditedTime;
    }

    protected void setLastEditedTime(LocalDateTime lastEditedTime) {
        this.lastEditedTime = lastEditedTime;
    }

    public double getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginSynsetId() {
        return originSynset;
    }

    public String getPointedSynsetId() {
        return pointedSynset;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public String getId() {
        return originSynset + "-" + pointedSynset;
    }

    @Override
    public String toString() {
        return originSynset + " → " + pointedSynset;
    }
}
