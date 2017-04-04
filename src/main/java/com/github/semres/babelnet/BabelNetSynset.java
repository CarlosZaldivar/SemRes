package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.gui.EdgesAlreadyLoadedException;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;

import java.io.IOException;
import java.util.*;

public class BabelNetSynset extends Synset {
    private BabelSynset babelSynset;
    private Set<BabelSynsetID> removedRelations = new HashSet<>();
    private Date lastUpdateDate;
    private boolean isDownloadedWithEdges;

    private BabelNetSynset(BabelNetSynset babelNetSynset) {
        super(babelNetSynset);
    }
    public BabelNetSynset(BabelSynset synset) {
        super(synset.getMainSense(BabelNetManager.getInstance().getJltLanguage()).getSenseString());

        setId(synset.getId().getID());

        String description;
        try {
            description = synset.getMainGloss(BabelNetManager.getInstance().getJltLanguage()).getGloss();
        } catch (IOException | NullPointerException e) {
            description = null;
        }
        this.description = description;
        babelSynset = synset;
    }

    public BabelNetSynset(String representation) {
        super(representation);
    }

    BabelNetSynset(String representation, Set<BabelSynsetID> removedRelations) {
        super(representation);
        this.removedRelations = removedRelations;
    }

    BabelNetSynset(String representation, Set<BabelSynsetID> removedRelations, boolean isDownloadedWithEdges) {
        this(representation, removedRelations);
        this.isDownloadedWithEdges = isDownloadedWithEdges;
    }

    public Set<BabelSynsetID> getRemovedRelations() {
        return removedRelations;
    }

    @Override
    public BabelNetSynset removeOutgoingEdge(String id) {
        BabelNetSynset newSynset = new BabelNetSynset(this);
        if (outgoingEdges.get(id) instanceof BabelNetEdge) {
            newSynset.removedRelations.add(((BabelNetSynset) outgoingEdges.get(id).getPointedSynset()).getBabelSynsetID());
        }
        newSynset.outgoingEdges.remove(id);
        return newSynset;
    }

    @Override
    protected BabelNetSynset addOutgoingEdge(Edge edge) {
        if (edge instanceof BabelNetEdge && removedRelations.contains(((BabelNetSynset) edge.getPointedSynset()).getBabelSynsetID())) {
            return null;
        }
        BabelNetSynset newSynset = new BabelNetSynset(this);
        newSynset.outgoingEdges.put(edge.getId(), edge);
        return newSynset;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void update() throws IOException, InvalidBabelSynsetIDException {
        if (babelSynset == null) {
            babelSynset = BabelNetManager.getInstance().getBabelSynset(getId());
        }

        BabelSense sense = babelSynset.getMainSense(BabelNetManager.getInstance().getJltLanguage());
        String representation = null;
        if (sense != null) {
            representation = sense.getSenseString();
        }
        String description;
        try {
            description = babelSynset.getMainGloss(BabelNetManager.getInstance().getJltLanguage()).toString();
        } catch (IOException e) {
            description = null;
        }

        this.representation = representation;
        this.description = description;
        lastUpdateDate = new Date();
    }

    public void loadEdgesFromBabelNet() throws IOException, InvalidBabelSynsetIDException {
        if (isDownloadedWithEdges) {
            throw new EdgesAlreadyLoadedException();
        }

        if (babelSynset == null) {
            babelSynset = BabelNetManager.getInstance().getBabelSynset(getId());
        }

        List<BabelSynsetIDRelation> babelEdges = babelSynset.getEdges();
        babelEdges.sort(Comparator.comparing(BabelSynsetIDRelation::getWeight));

        // Download only ten edges.
        int counter = 10;
        for (BabelSynsetIDRelation edge: babelEdges) {
            if (edgeIsRelevant(edge)) {
                addEdge(edge);
                --counter;
            }
            if (counter == 0) {
                break;
            }
        }
        isExpanded = true;
        isDownloadedWithEdges = true;
    }

    private void addEdge(BabelSynsetIDRelation edge) throws IOException {
        BabelNetSynset referencedSynset = new BabelNetSynset(BabelNet.getInstance().getSynset(edge.getBabelSynsetIDTarget()));
        BabelPointer babelPointer = edge.getPointer();
        Edge newEdge =
                new BabelNetEdge(referencedSynset, this, Edge.RelationType.HOLONYM, babelPointer.getName(), edge.getWeight());
        outgoingEdges.put(newEdge.getId(), newEdge);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private BabelSynsetID getBabelSynsetID() {
        if (babelSynset != null) {
            return babelSynset.getId();
        } else {
            try {
                return new BabelSynsetID(getId());
            } catch (InvalidBabelSynsetIDException e) {
                throw new RuntimeException("Invalid BabelNet ID");
            }
        }
    }

    private boolean edgeIsRelevant(BabelSynsetIDRelation edge) {
        if (removedRelations.contains(edge.getBabelSynsetIDTarget())) {
            return false;
        }

        if (edge.getWeight() == 0) {
            return false;
        }

        BabelPointer pointer = edge.getPointer();
        if (!(pointer.isHolonym() || pointer.isHypernym() || pointer.isMeronym() || pointer.isHyponym())) {
            return false;
        }

        return true;
    }

    public boolean isDownloadedWithEdges() {
        return isDownloadedWithEdges;
    }
}
