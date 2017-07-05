package com.github.semres;

import java.time.LocalDateTime;
import java.util.*;

public abstract class Synset {
    protected Map<String, Edge> outgoingEdges = new HashMap<>();
    protected String representation;
    protected String description;
    protected boolean isExpanded;
    protected LocalDateTime lastEditedTime;
    private final String id;

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
        this.isExpanded = copiedSynset.isExpanded;
    }

    public LocalDateTime getLastEditedTime() {
        return lastEditedTime;
    }

    void setLastEditedTime(LocalDateTime lastEditedTime) {
        this.lastEditedTime = lastEditedTime;
    }

    public boolean isExpanded() {
        return isExpanded;
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
        if (newEdges == null) {
            outgoingEdges.clear();
        } else {
            setOutgoingEdges(newEdges.values());
        }
    }

    protected void setOutgoingEdges(Collection<Edge> newEdges) {
        outgoingEdges.clear();
        for (Edge edge : newEdges) {
            outgoingEdges.put(edge.getId(), edge);
        }
        isExpanded = true;
    }

    abstract public Synset addOutgoingEdge(Edge edge);

    abstract public Synset removeOutgoingEdge(String id);

    @Override
    public String toString() {
        return representation;
    }
}
