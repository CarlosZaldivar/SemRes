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
    private Set<String> removedRelations = new HashSet<>();
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

    BabelNetSynset(String representation, Set<String> removedRelations) {
        super(representation);
        this.removedRelations = removedRelations;
    }

    BabelNetSynset(String representation, Set<String> removedRelations, boolean isDownloadedWithEdges) {
        this(representation, removedRelations);
        this.isDownloadedWithEdges = isDownloadedWithEdges;
    }

    public Set<String> getRemovedRelations() {
        return removedRelations;
    }

    @Override
    public BabelNetSynset removeOutgoingEdge(String id) {
        BabelNetSynset newSynset = new BabelNetSynset(this);
        if (outgoingEdges.get(id) instanceof BabelNetEdge) {
            newSynset.removedRelations.add(outgoingEdges.get(id).getPointedSynset());
        } else {
            throw new RuntimeException("No edge with specified ID to remove.");
        }
        newSynset.outgoingEdges.remove(id);
        return newSynset;
    }

    @Override
    protected BabelNetSynset addOutgoingEdge(Edge edge) {
        if (edge instanceof BabelNetEdge && removedRelations.contains(edge.getPointedSynset())) {
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

    public BabelNetSynset loadEdgesFromBabelNet() throws IOException {
        if (isDownloadedWithEdges) {
            throw new EdgesAlreadyLoadedException();
        }

        if (babelSynset == null) {
            try {
                babelSynset = BabelNetManager.getInstance().getBabelSynset(getId());
            } catch (InvalidBabelSynsetIDException e) {
                throw new Error(e.getMessage());
            }
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
        newSynset.isExpanded = true;
        newSynset.isDownloadedWithEdges = true;
        return newSynset;
    }

    private void addBabelNetEdge(BabelSynsetIDRelation edge) throws IOException {
        BabelPointer babelPointer = edge.getPointer();

        Edge.RelationType relationType = Edge.RelationType.valueOf(babelPointer.getRelationGroup().toString());

        Edge newEdge =
                new BabelNetEdge(edge.getBabelSynsetIDTarget().getID(), getId(), babelPointer.getName(), relationType, edge.getWeight());
        outgoingEdges.put(newEdge.getId(), newEdge);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private boolean edgeIsRelevant(BabelSynsetIDRelation edge) {
        return !removedRelations.contains(edge.getBabelSynsetIDTarget().getID());
    }

    public boolean isDownloadedWithEdges() {
        return isDownloadedWithEdges;
    }
}
