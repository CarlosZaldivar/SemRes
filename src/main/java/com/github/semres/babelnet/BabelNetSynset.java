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

    public List<Synset> loadEdgesFromBabelNet() throws IOException, InvalidBabelSynsetIDException {
        if (isDownloadedWithEdges) {
            throw new EdgesAlreadyLoadedException();
        }

        if (babelSynset == null) {
            babelSynset = BabelNetManager.getInstance().getBabelSynset(getId());
        }

        List<BabelSynsetIDRelation> babelEdges = babelSynset.getEdges();
        babelEdges.sort(Comparator.comparing(BabelSynsetIDRelation::getWeight));

        List<Synset> relatedSynsets = new ArrayList<>();
        // Download only ten edges.
        int counter = 10;
        for (BabelSynsetIDRelation edge: babelEdges) {
            if (edgeIsRelevant(edge)) {
                Synset relatedSynset = addBabelNetEdge(edge);
                relatedSynsets.add(relatedSynset);
                --counter;
            }
            if (counter == 0) {
                break;
            }
        }
        isExpanded = true;
        isDownloadedWithEdges = true;
        return relatedSynsets;
    }

    private Synset addBabelNetEdge(BabelSynsetIDRelation edge) throws IOException {
        BabelNetSynset referencedSynset = new BabelNetSynset(BabelNet.getInstance().getSynset(edge.getBabelSynsetIDTarget()));
        BabelPointer babelPointer = edge.getPointer();

        Edge.RelationType relationType = Edge.RelationType.valueOf(babelPointer.getRelationGroup().toString());

        Edge newEdge =
                new BabelNetEdge(referencedSynset.getId(), getId(), babelPointer.getName(), relationType, edge.getWeight());
        outgoingEdges.put(newEdge.getId(), newEdge);
        return referencedSynset;
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
