package com.github.semres;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    public Map<String, Synset> getSynsets() {
        return synsets;
    }

    private final Map<String, Synset> synsets = new HashMap<>();
    private final Map<String, Edge> edges = new HashMap<>();

    private final Map<String, Synset> newSynsets = new HashMap<>();
    private final Map<String, Edge> newEdges = new HashMap<>();
    private final Map<String, Synset> removedSynsets = new HashMap<>();
    private final Map<String, Edge> removedEdges = new HashMap<>();
    private Map<String, SynsetEdit> editedSynsets = new HashMap<>();

    private final Database attachedDatabase;

    Board(Database attachedDatabase) {
        this.attachedDatabase = attachedDatabase;
    }

    public List<Synset> loadSynsets(String searchPhrase) {
        List<Synset> synsetsFound = attachedDatabase.searchSynsets(searchPhrase);
        List<Synset> newSynsets = new ArrayList<>();
        for (Synset synset : synsetsFound) {
            if (synsets.get(synset.getId()) == null && removedSynsets.get(synset.getId()) == null) {
                newSynsets.add(synset);
                synsets.put(synset.getId(), synset);
            }
        }
        return newSynsets;
    }

    public void loadEdges(String synsetId) {
        Synset synset = synsets.get(synsetId);

        if (synset == null || !synset.getOutgoingEdges().isEmpty()) {
            return;
        }

        List<Edge> edges = attachedDatabase.getOutgoingEdges(synset);
        for (Edge edge : edges) {
            if (removedEdges.get(edge.getId()) != null) {
                continue;
            }

            if (synsets.containsKey(edge.getPointedSynset().getId())) {
                edge.setPointedSynset(synsets.get(edge.getPointedSynset().getId()));
            } else {
                synsets.put(edge.getPointedSynset().getId(), edge.getPointedSynset());
            }
            this.edges.put(edge.getId(), edge);
            synset.addOutgoingEdge(edge);
            edge.getPointedSynset().addPointingEdge(edge);
        }
    }

    public void addSynset(Synset newSynset) {
        if (newSynset.getId() == null) {
            newSynset.setId(getNewSynsetId());
        }
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
    }

    public void addEdge(Edge newEdge) {
        String originSynsetId = newEdge.getOriginSynset().getId();
        if (synsets.get(originSynsetId) == null) {
            addSynset(newEdge.getOriginSynset());
        } else {
            newEdge.setOriginSynset(synsets.get(originSynsetId));
        }
        String pointedSynsetId = newEdge.getPointedSynset().getId();
        if (synsets.get(pointedSynsetId) == null) {
            addSynset(newEdge.getPointedSynset());
        } else {
            newEdge.setPointedSynset(synsets.get(pointedSynsetId));
        }

        edges.put(newEdge.getId(), newEdge);
        newEdges.put(newEdge.getId(), newEdge);

        synsets.get(originSynsetId).addOutgoingEdge(newEdge);
    }

    public Synset getSynset(String id) {
        return synsets.get(id);
    }

    public List<Synset> searchLoadedSynsets(String searchPhrase) {
        List<Synset> synsetsFound = new ArrayList<>();

        for (Synset synset : synsets.values()) {
            if (synset.getRepresentation().toLowerCase().contains(searchPhrase.toLowerCase())) {
                synsetsFound.add(synset);
            }
        }
        return synsetsFound;
    }

    public void removeElement(String id) {
        if (synsets.containsKey(id)) {
            Synset removedSynset = synsets.get(id);
            for (Edge edge : removedSynset.getOutgoingEdges().values()) {
                edges.remove(edge.getId());
            }
            for (Edge edge : removedSynset.getPointingEdges().values()) {
                edges.remove(edge.getId());
            }
            synsets.remove(id);
            removedSynsets.put(id, removedSynset);
        } else if (edges.containsKey(id)) {
            Edge removedEdge = edges.get(id);
            removedEdge.getPointedSynset().removePointingEdge(removedEdge);
            removedEdge.getOriginSynset().removeOutgoingEdge(removedEdge);
            edges.remove(id);
            removedEdges.put(id, removedEdge);
        }
    }

    public void save() {
        for (Synset synset : newSynsets.values()) {
            attachedDatabase.addSynset(synset);
        }
        newSynsets.clear();
        for (Edge edge : newEdges.values()) {
            attachedDatabase.addEdge(edge);
        }
        newEdges.clear();
        for (Edge edge : removedEdges.values()) {
            attachedDatabase.removeEdge(edge);
        }
        removedEdges.clear();
        for (Synset synset : removedSynsets.values()) {
            attachedDatabase.removeSynset(synset);
        }
        removedSynsets.clear();
    }

    public String export() {
        return attachedDatabase.export();
    }

    /**
     * Generate unique synset id.
     * @return Unique synset id.
     */
    private String getNewSynsetId() {
        SecureRandom random = new SecureRandom();
        String id;
        while (true) {
            id = new BigInteger(130, random).toString(32);
            if (!synsets.containsKey(id)) {
                break;
            }
        }
        return id;
    }
}

class SynsetEdit {
    private final Synset original;
    private final Synset edited;

    public Synset getOriginal() {
        return original;
    }

    public Synset getEdited() {
        return edited;
    }

    public SynsetEdit(Synset original, Synset edited) {
        this.original = original;
        this.edited = edited;
    }
}
