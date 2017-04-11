package com.github.semres;

import java.util.HashMap;
import java.util.Map;

class SynsetEdit {
    private final Synset original;
    private final Map<String, Edge> addedEdges;
    private final Map<String, Edge> removedEdges;
    private Synset edited;
    SynsetEdit(Synset original, Synset edited) {
        this.original = original;
        this.edited = edited;
        this.addedEdges = new HashMap<>();
        this.removedEdges = new HashMap<>();
    }

    public void addEdge(Edge edge) {
        addedEdges.put(edge.getId(), edge);
    }

    void removeEdge(Edge edge) {
        removedEdges.put(edge.getId(), edge);
    }

    Synset getOriginal() {
        return original;
    }

    Synset getEdited() {
        return edited;
    }

    public void setEdited(Synset edited) {
        this.edited = edited;
    }

    Map<String, Edge> getAddedEdges() {
        return addedEdges;
    }

    Map<String, Edge> getRemovedEdges() {
        return removedEdges;
    }
}