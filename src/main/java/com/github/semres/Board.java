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

    private Map<String, Synset> synsets = new HashMap<>();
    private Map<String, Boolean> synsetsLoadedState = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();

    private Map<String, Synset> newSynsets = new HashMap<>();
    private Map<String, Edge> newEdges = new HashMap<>();
    private Map<String, Synset> removedSynsets = new HashMap<>();
    private Map<String, Edge> removedEdges = new HashMap<>();

    private Database attachedDatabase;

    public Board(Database attachedDatabase) {
        this.attachedDatabase = attachedDatabase;
    }

    public List<Synset> loadSynsets(String searchPhrase) {
        List<Synset> synsetsFound = attachedDatabase.searchSynsets(searchPhrase);
        List<Synset> newSynsets = new ArrayList<>();
        for (Synset synset : synsetsFound) {
            if (synsets.get(synset.getId()) == null) {
                newSynsets.add(synset);
                synsets.put(synset.getId(), synset);
            }
        }
        return newSynsets;
    }

    public void loadEdges(String synsetId) {
        Synset synset = synsets.get(synsetId);
        if (synset != null) {
            List<Edge> edges = attachedDatabase.getOutgoingEdges(synset);
            for (Edge edge : edges) {
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
    }

    public void addSynset(Synset newSynset) {
        if (newSynset.getId() == null) {
            newSynset.setId(getNewSynsetId());
        }
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
    }

    public void addEdge(Edge newEdge) {
        edges.put(newEdge.getId(), newEdge);
        newEdges.put(newEdge.getId(), newEdge);

        synsets.get(newEdge.getOriginSynset().getId()).addOutgoingEdge(newEdge);
    }

    public Synset getSynset(String id) {
        return synsets.get(id);
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
