package com.github.semres;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    private final Map<String, Synset> synsets = new HashMap<>();
    private final Map<String, Synset> newSynsets = new HashMap<>();
    private final Map<String, Synset> removedSynsets = new HashMap<>();
    private final Map<String, SynsetEdit> synsetEdits = new HashMap<>();
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

        if (synset == null || synset.isExpanded()) {
            return;
        }

        List<Edge> edges = attachedDatabase.getOutgoingEdges(synset);
        List<Edge> filteredEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edgeIsRemoved(synset, edge)) {
                continue;
            }

            if (synsets.containsKey(edge.getPointedSynset().getId())) {
                edge.setPointedSynset(synsets.get(edge.getPointedSynset().getId()));
            } else {
                synsets.put(edge.getPointedSynset().getId(), edge.getPointedSynset());
            }

            filteredEdges.add(edge);
        }
        synset.setOutgoingEdges(filteredEdges);
    }

    private boolean edgeIsRemoved(Synset synset, Edge edge) {
        if (synsetEdits.get(synset.getId()) == null) {
            return false;
        }
        if (synsetEdits.get(synset.getId()).getRemovedEdges().get(edge.getId()) == null) {
            return false;
        }
        return true;
    }

    public void addSynset(Synset newSynset) {
        if (newSynset.getId() == null) {
            newSynset.setId(attachedDatabase.generateNewSynsetId());
        }
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
    }

    public void addEdge(Edge newEdge) {
        String originSynsetId = newEdge.getOriginSynset().getId();
        if (synsets.get(originSynsetId) == null) {
            addSynset(newEdge.getOriginSynset());
        } else {
            Synset originalSynset = synsets.get(originSynsetId);
            Synset editedSynset = originalSynset.addOutgoingEdge(newEdge);

            // Return if no edition was made.
            if (editedSynset == null) {
                return;
            }

            synsets.put(originSynsetId, editedSynset);
            newEdge.setOriginSynset(editedSynset);

            SynsetEdit synsetEdit = new SynsetEdit(originalSynset, editedSynset);
            synsetEdit.addEdge(newEdge);
            synsetEdits.put(originSynsetId, synsetEdit);
        }

        String pointedSynsetId = newEdge.getPointedSynset().getId();
        if (synsets.get(pointedSynsetId) == null) {
            addSynset(newEdge.getPointedSynset());
        } else {
            newEdge.setPointedSynset(synsets.get(pointedSynsetId));
        }

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

    public void removeNode(String id) {
        if (synsets.containsKey(id)) {
            Synset removedSynset = synsets.get(id);
            synsets.remove(id);
            removedSynsets.put(id, removedSynset);
        }
    }

    public void removeEdge(String id) {
        Synset originSynset = synsets.get(extractOriginSynsetId(id));
        if (originSynset != null) {
            if (originSynset.getOutgoingEdges().containsKey(id)) {
                Edge removedEdge = originSynset.getOutgoingEdges().get(id);
                Synset editedSynset = originSynset.removeOutgoingEdge(id);

                SynsetEdit synsetEdit = new SynsetEdit(originSynset, editedSynset);
                synsetEdit.removeEdge(removedEdge);
                synsetEdits.put(originSynset.getId(), synsetEdit);
                synsets.put(editedSynset.getId(), editedSynset);
            }
        }
    }

    public void save() {
        for (Synset synset : newSynsets.values()) {
            attachedDatabase.addSynset(synset);
            for (Edge edge : synset.getOutgoingEdges().values()) {
                attachedDatabase.addEdge(edge);
            }
        }
        newSynsets.clear();

        for (SynsetEdit synsetEdit : synsetEdits.values()) {
            for (Edge edge : synsetEdit.getAddedEdges().values()) {
                attachedDatabase.addEdge(edge);
            }
            for (Edge edge : synsetEdit.getRemovedEdges().values()) {
                attachedDatabase.removeEdge(edge);
            }
            attachedDatabase.editSynset(synsetEdit.getEdited(), synsetEdit.getOriginal());
        }
        synsetEdits.clear();

        for (Synset synset : removedSynsets.values()) {
            attachedDatabase.removeSynset(synset);
        }
        removedSynsets.clear();
    }

    public String export() {
        Model model = attachedDatabase.getAllStatements();
        StringWriter buffer = new StringWriter();
        Rio.write(model, buffer, RDFFormat.RDFXML);
        return buffer.toString();
    }

    private String extractOriginSynsetId(String edgeId) {
        return edgeId.split("-")[0];
    }
}