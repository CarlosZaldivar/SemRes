package com.github.semres;

import java.time.LocalDateTime;
import java.util.*;

public abstract class Synset {
    private final String id;
    protected Map<String, Edge> outgoingEdges = new HashMap<>();
    protected String representation;
    protected String description;
    protected boolean hasDatabaseEdgesLoaded;
    protected LocalDateTime lastEditedTime;
    protected Synset(String representation, String id) {
        this.representation = representation;
        this.id = id;
        this.description = null;
    }

    protected  Synset(String representation, String id, String description) {
        this.representation = representation;
        this.id = id;
        this.description = description;
    }

    protected Synset(Synset copiedSynset) {
        this.representation = copiedSynset.representation;
        this.description = copiedSynset.description;
        this.id = copiedSynset.id;
        this.outgoingEdges = new HashMap<>(copiedSynset.outgoingEdges);
        this.hasDatabaseEdgesLoaded = copiedSynset.hasDatabaseEdgesLoaded;
    }

    public boolean hasDatabaseEdgesLoaded() {
        return hasDatabaseEdgesLoaded;
    }

    public LocalDateTime getLastEditedTime() {
        return lastEditedTime;
    }

    void setLastEditedTime(LocalDateTime lastEditedTime) {
        this.lastEditedTime = lastEditedTime;
    }

    public String getRepresentation() {
        return representation;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public Map<String, Edge> getOutgoingEdges() {
        return new HashMap<>(outgoingEdges);
    }

    protected void setOutgoingEdges(Map<String, Edge> newEdges) {
        outgoingEdges = new HashMap<>(newEdges);
    }

    abstract public Synset addOutgoingEdge(Edge edge);

    abstract public Synset removeOutgoingEdge(String id);

    public abstract Synset changeOutgoingEdge(Edge edge);

    @Override
    public String toString() {
        return representation;
    }
}
