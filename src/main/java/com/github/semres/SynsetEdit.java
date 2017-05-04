package com.github.semres;

import java.util.HashMap;
import java.util.Map;

class SynsetEdit {
    private final Synset original;
    private final Map<String, Edge> addedEdges;
    private final Map<String, Edge> removedEdges;
    private final Map<String, EdgeEdit> edgeEdits;
    private Synset edited;
    SynsetEdit(Synset original, Synset edited) {
        this.original = original;
        this.edited = edited;
        this.addedEdges = new HashMap<>();
        this.removedEdges = new HashMap<>();
        this.edgeEdits = new HashMap<>();
    }

    void addEdge(Edge edge) {
        addedEdges.put(edge.getId(), edge);
    }

    void removeEdge(Edge edge) {
        removedEdges.put(edge.getId(), edge);
        if (edgeEdits.containsKey(edge.getId())) {
            edgeEdits.remove(edge.getId());
        }
    }

    void editEdge(Edge original, Edge edited) {
        if (edgeEdits.containsKey(original.getId())) {
            edgeEdits.get(original.getId()).setEdited(edited);
        } else {
            edgeEdits.put(original.getId(), new EdgeEdit(original, edited));
        }
    }

    Synset getOriginal() {
        return original;
    }

    Synset getEdited() {
        return edited;
    }

    void setEdited(Synset edited) {
        this.edited = edited;
    }

    Map<String, Edge> getAddedEdges() {
        return addedEdges;
    }

    Map<String, Edge> getRemovedEdges() {
        return removedEdges;
    }

    Map<String, EdgeEdit> getEdgeEdits() {
        return edgeEdits;
    }
}