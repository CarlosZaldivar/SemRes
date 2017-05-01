package com.github.semres;

import com.github.semres.gui.IDAlreadyTakenException;
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
            if (!synsets.containsKey(synset.getId()) && !removedSynsets.containsKey(synset.getId())) {
                synsets.put(synset.getId(), synset);
                newSynsets.add(synset);
            }
        }
        return newSynsets;
    }

    public Synset loadSynset(String id) {
        if (synsets.containsKey(id)) {
            return synsets.get(id);
        }

        if (!removedSynsets.containsKey(id)) {
            Synset synset = attachedDatabase.getSynset(id);
            synsets.put(synset.getId(), synset);
            return synset;
        } else {
            return null;
        }
    }

    public List<Edge> loadEdges(String synsetId) {
        Synset synset = synsets.get(synsetId);

        if (synset == null) {
            throw new RuntimeException("No synset with specified ID");
        }
        if (synset.isExpanded()) {
            throw new RuntimeException("Edges already loaded");
        }

        List<Edge> edges = attachedDatabase.getOutgoingEdges(synset);
        List<Edge> filteredEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (isEdgeRemoved(edge)) {
                continue;
            }

            if (!synsets.containsKey(edge.getPointedSynset())) {
                Synset pointedSynset = attachedDatabase.getSynset(edge.getPointedSynset());
                synsets.put(pointedSynset.getId(), pointedSynset);
            }

            filteredEdges.add(edge);
        }
        synset.setOutgoingEdges(filteredEdges);
        return filteredEdges;
    }

    private boolean isEdgeRemoved(Edge edge) {
        String originSynsetId = edge.getOriginSynset();
        if (!synsetEdits.containsKey(originSynsetId)) {
            return false;
        }
        if (!synsetEdits.get(originSynsetId).getRemovedEdges().containsKey(edge.getId())) {
            return false;
        }
        return true;
    }

    public void addSynset(Synset newSynset) {
        if (newSynset.getId() == null) {
            newSynset.setId(attachedDatabase.generateNewSynsetId());
        } else if (isIdAlreadyTaken(newSynset.getId())) {
            throw new IDAlreadyTakenException();
        }
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
    }

    public void addEdge(Edge newEdge) {
        if (edgeAlreadyExists(newEdge.getId())) {
            throw new IDAlreadyTakenException();
        }

        String originSynsetId = newEdge.getOriginSynset();
        String pointedSynsetId = newEdge.getPointedSynset();

        if (synsets.get(originSynsetId) == null) {
            throw new RuntimeException("Trying to add edge without corresponding origin synset on the board.");
        }
        if (synsets.get(pointedSynsetId) == null) {
            throw new RuntimeException("Trying to add edge without corresponding pointed synset on the board.");
        }


        Synset originalSynset = synsets.get(originSynsetId);
        Synset editedSynset = originalSynset.addOutgoingEdge(newEdge);

        // Return if no edition was made.
        if (editedSynset == null) {
            return;
        }

        synsets.put(originSynsetId, editedSynset);

        SynsetEdit synsetEdit = synsetEdits.get(originSynsetId);
        if (synsetEdit == null) {
            synsetEdit = new SynsetEdit(originalSynset, editedSynset);
            synsetEdits.put(originSynsetId, synsetEdit);
        } else {
            synsetEdit.setEdited(editedSynset);
        }
        synsetEdit.addEdge(newEdge);
    }

    public Synset getSynset(String id) {
        return synsets.get(id);
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

                SynsetEdit synsetEdit = synsetEdits.get(originSynset.getId());
                if (synsetEdit == null) {
                    synsetEdit = new SynsetEdit(originSynset, editedSynset);
                    synsetEdits.put(originSynset.getId(), synsetEdit);
                } else {
                    synsetEdit.setEdited(editedSynset);
                }
                synsetEdit.removeEdge(removedEdge);
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

    public boolean isIdAlreadyTaken(String id) {
        if (synsets.containsKey(id)) {
            return true;
        }
        if (attachedDatabase.hasSynset(id)) {
            return true;
        }
        return false;
    }

    private String extractOriginSynsetId(String edgeId) {
        return edgeId.split("-")[0];
    }

    private boolean edgeAlreadyExists(String edgeId) {
        String originSynsetId = extractOriginSynsetId(edgeId);
        if (synsets.containsKey(originSynsetId)) {
            if (synsets.get(originSynsetId).getOutgoingEdges().containsKey(edgeId)) {
                return true;
            }
        }

        if (attachedDatabase.hasEdge(edgeId)) {
            return true;
        }
        return false;
    }
}