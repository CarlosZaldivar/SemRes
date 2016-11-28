package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.Synset;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BabelNetSynset extends Synset {
    private final BabelSynset babelSynset;
    private Map<String, Edge> removedEdges = new HashMap<>();
    private Date lastUpdateDate;

    public BabelNetSynset(BabelSynset synset) {
        super(synset.getId().toString());

        BabelSense sense = synset.getMainSense(BabelNetManager.getInstance().getJltLanguage());
        String representation = null;
        if (sense != null) {
            representation = sense.getSenseString();
        }

        String description;
        try {
            description = synset.getMainGloss(BabelNetManager.getInstance().getJltLanguage()).toString();
        } catch (IOException e) {
            description = null;
        }
        this.representation = representation;
        this.description = description;
        babelSynset = synset;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void update() throws IOException {
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

        List<BabelSynsetIDRelation> babelEdges = babelSynset.getEdges();
        for (BabelSynsetIDRelation edge: babelEdges) {
            addEdge(edge);
        }
    }

    private void addEdge(BabelSynsetIDRelation edge) throws IOException {
        if (edge.getWeight() > 0) {
            BabelNetSynset referencedSynset = new BabelNetSynset(BabelNet.getInstance().getSynset(edge.getBabelSynsetIDTarget()));
            BabelPointer babelPointer = edge.getPointer();
            Edge newEdge =
                    new BabelNetEdge(referencedSynset, this, Edge.RelationType.HOLONYM, babelPointer.getName(), edge.getWeight());
            if (!removedEdges.containsKey(newEdge.getId())) {
                outgoingEdges.put(newEdge.getId(), newEdge);
            }
        }
    }
}
