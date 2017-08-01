package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.Synset;

import java.time.LocalDateTime;
import java.util.*;

public class BabelNetSynset extends Synset {
    private Set<String> removedRelations = new HashSet<>();
    boolean downloadedWithEdges;

    public BabelNetSynset(BabelNetSynset babelNetSynset) {
        super(babelNetSynset);
        this.downloadedWithEdges = babelNetSynset.downloadedWithEdges;
        this.removedRelations = new HashSet<>(babelNetSynset.removedRelations);
    }

    public BabelNetSynset(String representation, String id) {
        super(representation, id);
    }

    public BabelNetSynset(String representation, String id, String description) {
        super(representation, id, description);
    }

    private BabelNetSynset(String representation, String id, String description, Set<String> removedRelations) {
        super(representation, id, description);
        this.removedRelations = removedRelations;
    }

    private BabelNetSynset(String representation, String id, String description, Set<String> removedRelations, boolean isDownloadedWithEdges) {
        this(representation, id, description, removedRelations);
        this.downloadedWithEdges = isDownloadedWithEdges;
    }

    public BabelNetSynset(String representation, String id, String description, boolean isDownloadedWithEdges) {
        this(representation, id, description);
        this.downloadedWithEdges = isDownloadedWithEdges;
    }

    public BabelNetSynset(String representation, String id, boolean isDownloadedWithEdges) {
        this(representation, id);
        this.downloadedWithEdges = isDownloadedWithEdges;
    }

    public BabelNetSynset(String representation, String id, String description, Set<String> removedRelations, boolean edgesLoaded, LocalDateTime lastEditedTime) {
        this(representation, id, description, removedRelations, edgesLoaded);
        this.lastEditedTime = lastEditedTime;
    }

    public Set<String> getRemovedRelations() {
        return removedRelations;
    }

    @Override
    public BabelNetSynset removeOutgoingEdge(String id) {
        BabelNetSynset newSynset = new BabelNetSynset(this);
        if (outgoingEdges.get(id) instanceof BabelNetEdge) {
            newSynset.removedRelations.add(outgoingEdges.get(id).getPointedSynsetId());
        } else {
            throw new RuntimeException("No edge with specified ID to remove.");
        }
        newSynset.outgoingEdges.remove(id);
        return newSynset;
    }

    @Override
    public BabelNetSynset addOutgoingEdge(Edge edge) {
        if (edge instanceof BabelNetEdge && removedRelations.contains(edge.getPointedSynsetId())) {
            throw new RuntimeException("Trying to add removed relation.");
        }
        BabelNetSynset newSynset = new BabelNetSynset(this);
        newSynset.outgoingEdges.put(edge.getId(), edge);
        return newSynset;
    }

    public boolean isDownloadedWithEdges() {
        return downloadedWithEdges;
    }

    @Override
    public BabelNetSynset changeOutgoingEdge(Edge edge) {
        if (!outgoingEdges.containsKey(edge.getId())) {
            throw new RuntimeException("No edge with specified ID.");
        }

        BabelNetSynset newSynset = new BabelNetSynset(this);
        newSynset.outgoingEdges.put(edge.getId(), edge);
        return newSynset;
    }

    @Override
    protected void setOutgoingEdges(Map<String, Edge> newEdges) {
        super.setOutgoingEdges(newEdges);
    }
}
