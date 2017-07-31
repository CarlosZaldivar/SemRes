package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.RelationType;
import com.github.semres.Synset;
import com.github.semres.gui.EdgesAlreadyLoadedException;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class BabelNetSynset extends Synset {
    private BabelSynset babelSynset;
    private Set<String> removedRelations = new HashSet<>();
    private boolean downloadedWithEdges;
    private BabelNetManager babelNetManager;

    private BabelNetSynset(BabelNetSynset babelNetSynset) {
        super(babelNetSynset);
        this.downloadedWithEdges = babelNetSynset.downloadedWithEdges;
        this.removedRelations = new HashSet<>(babelNetSynset.removedRelations);
        babelNetManager = babelNetSynset.babelNetManager;
    }

    public BabelNetSynset(BabelSynset synset) {
        super(synset.getMainSense(BabelNetManager.getJltLanguage()).getSenseString(), synset.getId().getID());
        babelNetManager = new BabelNetManager();

        String description;
        try {
            description = synset.getMainGloss(BabelNetManager.getJltLanguage()).getGloss();
        } catch (IOException | NullPointerException e) {
            description = null;
        }
        this.description = description;
        babelSynset = synset;
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

    public void setBabelNetManager(BabelNetManager babelNetManager) {
        this.babelNetManager = babelNetManager;
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

    public BabelNetSynset loadEdgesFromBabelNet() throws IOException {
        if (downloadedWithEdges) {
            throw new EdgesAlreadyLoadedException();
        }

        if (babelSynset == null) {
            babelSynset = babelNetManager.getBabelSynset(getId());
        }

        BabelNetSynset newSynset = new BabelNetSynset(this);

        List<BabelSynsetIDRelation> babelEdges = babelSynset.getEdges();
        babelEdges.sort(Comparator.comparing(BabelSynsetIDRelation::getWeight).reversed());

        // Download all edges with weight > 0 and if there's less than 10 of them, download edges with weight = 0 too.
        int counter = 10;
        for (BabelSynsetIDRelation edge: babelEdges) {
            if (edgeIsRelevant(edge)) {
                newSynset.addBabelNetEdge(edge);
                --counter;
            }
            if (counter <= 0 && edge.getWeight() == 0) {
                break;
            }
        }
        newSynset.downloadedWithEdges = true;
        return newSynset;
    }

    private void addBabelNetEdge(BabelSynsetIDRelation edge) throws IOException {
        BabelPointer babelPointer = edge.getPointer();

        RelationType relationType = new RelationType(babelPointer.getRelationGroup().toString(), "BabelNet");

        Edge newEdge =
                new BabelNetEdge(edge.getBabelSynsetIDTarget().getID(), getId(), babelPointer.getName(), relationType, edge.getWeight());
        outgoingEdges.put(newEdge.getId(), newEdge);
    }

    private boolean edgeIsRelevant(BabelSynsetIDRelation edge) {
        return !removedRelations.contains(edge.getBabelSynsetIDTarget().getID());
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

    public boolean isDownloadedWithEdges() {
        return downloadedWithEdges;
    }
}
