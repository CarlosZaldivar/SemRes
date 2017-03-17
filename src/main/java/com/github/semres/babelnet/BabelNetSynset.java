package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.Synset;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;

import java.io.IOException;
import java.util.*;

public class BabelNetSynset extends Synset {
    private BabelSynset babelSynset;

    public Set<BabelSynsetID> getRemovedRelations() {
        return removedRelations;
    }

    private Set<BabelSynsetID> removedRelations = new HashSet<>();
    private Date lastUpdateDate;

    BabelNetSynset(BabelSynset synset) {
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

    @Override
    public void removeOutgoingEdge(String id) {
        if (outgoingEdges.get(id) instanceof BabelNetEdge) {
            removedRelations.add(((BabelNetSynset) outgoingEdges.get(id).getPointedSynset()).getBabelSynsetID());
        }
        super.removeOutgoingEdge(id);
    }

    public BabelNetSynset(String representation) {
        super(representation);
    }

    BabelNetSynset(String representation, Set<BabelSynsetID> removedRelations) {
        super(representation);
        this.removedRelations = removedRelations;
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
}
