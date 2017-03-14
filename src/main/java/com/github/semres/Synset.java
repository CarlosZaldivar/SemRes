package com.github.semres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Synset {
    private String id;
    protected Map<String, Edge> outgoingEdges = new HashMap<>();
    private Map<String, Edge> pointingEdges = new HashMap<>();
    protected String representation;
    protected String description;

    protected Synset(String representation) {
        this.representation = representation;
    }

    public void setId(String id) {
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

    public Map<String, Edge> getOutgoingEdges() {
        return new HashMap<>(outgoingEdges);
    }

    void setOutgoingEdges(Map<String, Edge> newEdges) {
        outgoingEdges = (newEdges == null) ? new HashMap<>() : new HashMap<>(newEdges);
    }

    public void setOutgoingEdges(List<Edge> newEdges) {
        for (Edge edge : new ArrayList<>(newEdges)) {
            outgoingEdges.put(edge.getId(), edge);
        }
    }

    Map<String, Edge> getPointingEdges() {
        return new HashMap<>(pointingEdges);
    }

    public void setPointingEdges(Map<String, Edge> newEdges) {
        pointingEdges = (newEdges == null) ? new HashMap<>() : new HashMap<>(newEdges);
    }

    public void setPointingEdges(List<Edge> newEdges) {
        for (Edge edge : new ArrayList<>(newEdges)) {
            pointingEdges.put(edge.getId(), edge);
        }
    }

    void addOutgoingEdge(Edge edge) {
        outgoingEdges.put(edge.getId(), edge);
    }

    void addPointingEdge(Edge edge) { pointingEdges.put(edge.getId(), edge); }

    void removeOutgoingEdge(Edge edge) {
        removeOutgoingEdge(edge.getId());
    }

    void removePointingEdge(Edge edge) {
        removePointingEdge(edge.getId());
    }

    protected void removeOutgoingEdge(String id) {
        outgoingEdges.remove(id);
    }

    private void removePointingEdge(String id) {
        pointingEdges.remove(id);
    }
}
