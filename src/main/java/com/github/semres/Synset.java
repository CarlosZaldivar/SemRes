package com.github.semres;

import java.util.HashMap;
import java.util.Map;

public abstract class Synset {
    private final String id;
    protected Map<String, Edge> edges = new HashMap<>();
    protected String representation;
    protected String description;

    public Synset(String id) {
        this.id = id;
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

    public Map<String, Edge> getEdges() {
        return new HashMap<>(edges);
    }

    public void setEdges(Map<String, Edge> newEdges) {
        edges = (newEdges == null) ? new HashMap<>() : new HashMap<>(newEdges);
    }
}
