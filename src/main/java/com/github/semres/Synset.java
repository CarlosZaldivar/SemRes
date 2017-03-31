package com.github.semres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Synset {
    private String id;
    protected Map<String, Edge> outgoingEdges = new HashMap<>();
    protected String representation;
    protected String description;

    protected Synset(String representation) {
        this.representation = representation;
    }
    protected Synset(Synset copiedSynset) {
        this.representation = copiedSynset.representation;
        this.description = copiedSynset.description;
        this.id = copiedSynset.id;
        this.outgoingEdges = new HashMap<>(copiedSynset.outgoingEdges);
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

    protected void setOutgoingEdges(List<Edge> newEdges) {
        for (Edge edge : new ArrayList<>(newEdges)) {
            outgoingEdges.put(edge.getId(), edge);
        }
    }

    abstract protected Synset addOutgoingEdge(Edge edge);

    Synset removeOutgoingEdge(Edge edge) {
        return removeOutgoingEdge(edge.getId());
    }

    abstract protected Synset removeOutgoingEdge(String id);
}
