package com.github.semres;

import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.babelnet.CommonIRI;
import com.github.semres.gui.IDAlreadyTakenException;
import com.github.semres.user.UserEdge;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Board {
    private final Map<String, Synset> synsets = new HashMap<>();
    private final Map<String, Synset> newSynsets = new HashMap<>();
    private final Map<String, Synset> removedSynsets = new HashMap<>();
    private final Map<String, SynsetEdit> synsetEdits = new HashMap<>();
    private final Database attachedDatabase;
    private BabelNetManager babelNetManager;

    Board(Database attachedDatabase) {
        this.attachedDatabase = attachedDatabase;
        babelNetManager = new BabelNetManager();
    }

    public Board(Database attachedDatabase, BabelNetManager babelNetManager) {
        this.attachedDatabase = attachedDatabase;
        this.babelNetManager = babelNetManager;
    }

    public void setBabelNetManager(BabelNetManager babelNetManager) {
        this.babelNetManager = babelNetManager;
    }

    public List<Synset> loadSynsets(String searchPhrase) {
        List<Synset> synsetsFound = attachedDatabase.searchSynsets(searchPhrase);
        List<Synset> newSynsets = new ArrayList<>();
        for (Synset synset : synsetsFound) {
            if (!synsetEdits.containsKey(synset.getId()) && !removedSynsets.containsKey(synset.getId())) {
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

    public List<Edge> downloadBabelNetEdges(String synsetId) throws IOException {
        ((BabelNetSynset) synsets.get(synsetId)).setBabelNetManager(babelNetManager);
        BabelNetSynset updatedSynset = ((BabelNetSynset) synsets.get(synsetId)).loadEdgesFromBabelNet();

        SynsetEdit synsetEdit;
        if (!synsetEdits.containsKey(synsetId)) {
            synsetEdit = new SynsetEdit(synsets.get(synsetId), updatedSynset);
            synsetEdits.put(synsetId, synsetEdit);
        } else {
            synsetEdit = synsetEdits.get(synsetId);
            synsetEdit.setEdited(updatedSynset);
        }
        synsets.put(updatedSynset.getId(), updatedSynset);

        // Get BabelNetEdges
        List<Edge> edges = updatedSynset.getOutgoingEdges().values().stream()
                .filter((edge) -> edge instanceof BabelNetEdge)
                .collect(Collectors.toList());

        for (Edge edge : edges) {
            synsetEdit.addEdge(edge);
            String pointedSynsetId = edge.getPointedSynset();

            // If pointed synset is not already on board or in the database download it from BabelNet.
            if (!synsets.containsKey(pointedSynsetId)) {
                if (attachedDatabase.hasSynset(pointedSynsetId)) {
                    loadSynset(pointedSynsetId);
                } else {
                    addSynset(babelNetManager.getSynset(pointedSynsetId));
                }
            }
        }
        return edges;
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

    public Edge getEdge(String id) {
        Synset synset = synsets.get(extractOriginSynsetId(id));
        return synset.getOutgoingEdges().get(id);
    }

    // Edit synset's representation or description.
    public void editSynset(Synset originalSynset, Synset editedSynset) {
        SynsetEdit synsetEdit;
        if (!synsetEdits.containsKey(originalSynset.getId())) {
            synsetEdit = new SynsetEdit(originalSynset, editedSynset);
            synsetEdits.put(originalSynset.getId(), synsetEdit);
        } else {
            synsetEdit = synsetEdits.get(originalSynset.getId());
            synsetEdit.setEdited(editedSynset);
        }
        synsets.put(originalSynset.getId(), editedSynset);
    }

    public void editEdge(UserEdge oldEdge, UserEdge editedEdge) {
        SynsetEdit synsetEdit;
        String originSynsetId = oldEdge.getOriginSynset();
        if (!synsetEdits.containsKey(originSynsetId)) {
            synsetEdit = new SynsetEdit(synsets.get(originSynsetId), synsets.get(originSynsetId));
            synsetEdits.put(originSynsetId, synsetEdit);
        } else {
            synsetEdit = synsetEdits.get(originSynsetId);
        }

        synsetEdit.editEdge(oldEdge, editedEdge);
        Synset synset  =synsets.get(originSynsetId);
        Map<String, Edge> outgoingEdges = synset.getOutgoingEdges();
        outgoingEdges.put(oldEdge.getId(), editedEdge);
        synset.setOutgoingEdges(outgoingEdges);
    }

    public void removeSynset(String id) {
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
            for (EdgeEdit edgeEdit : synsetEdit.getEdgeEdits().values()) {
                attachedDatabase.editEdge(edgeEdit.getEdited(), edgeEdit.getOriginal());
            }
            attachedDatabase.editSynset(synsetEdit.getEdited(), synsetEdit.getOriginal());
        }
        synsetEdits.clear();

        for (Synset synset : removedSynsets.values()) {
            attachedDatabase.removeSynset(synset);
        }
        removedSynsets.clear();
    }

    public List<SynsetUpdate> checkForUpdates() throws IOException {
        if (isBoardEdited()) {
            throw new RuntimeException("Cannot check for updates with unsaved changes.");
        }

        List<Synset> synsets = attachedDatabase.getSynsets(CommonIRI.BABELNET_SYNSET);
        List<SynsetUpdate> updates = new ArrayList<>();
        for (Synset synset : synsets) {
            BabelNetSynset originalSynset = (BabelNetSynset) synset;
            originalSynset.setOutgoingEdges(attachedDatabase.getOutgoingEdges(originalSynset));
            BabelNetSynset updatedSynset = babelNetManager.getSynset(originalSynset.getId());

            // If synset is not found in BabelNet, remove it.
            if (updatedSynset == null) {
                updates.add(new SynsetUpdate(originalSynset, null, null));
                continue;
            }

            updatedSynset = updatedSynset.loadEdgesFromBabelNet();

            Map<String, BabelNetSynset> relatedSynsets = new HashMap<>();

            for (Edge edge : originalSynset.getOutgoingEdges().values().stream().filter((e) -> e instanceof BabelNetEdge).collect(Collectors.toList())) {
                relatedSynsets.put(edge.getPointedSynset(), (BabelNetSynset) loadSynset(edge.getPointedSynset()));
            }

            for (Edge edge : updatedSynset.getOutgoingEdges().values()) {
                if (isIdAlreadyTaken(edge.getPointedSynset())) {
                    relatedSynsets.put(edge.getPointedSynset(), (BabelNetSynset) loadSynset(edge.getPointedSynset()));
                } else {
                    relatedSynsets.put(edge.getPointedSynset(), babelNetManager.getSynset(edge.getPointedSynset()));
                }
            }

            SynsetUpdate update = new SynsetUpdate(originalSynset, updatedSynset, relatedSynsets);
            if (update.isSynsetUpdated()) {
                updates.add(update);
            }
        }
        return updates;
    }

    public void update(List<SynsetUpdate> updates) {
        if (isBoardEdited()) {
            throw new RuntimeException("Cannot update with unsaved changes.");
        }

        for (SynsetUpdate update : updates) {
            if (update.getUpdatedSynset() == null) {
                attachedDatabase.removeSynset(update.getOriginalSynset());
                synsets.remove(update.getOriginalSynset().getId());
                continue;
            }

            if (update.isSynsetDataUpdated()) {
                attachedDatabase.editSynset(update.getUpdatedSynset(), update.getOriginalSynset());
            }

            for (Edge edge : update.getRemovedEdges().values()) {
                attachedDatabase.removeEdge(edge);
            }

            for (Edge edge : update.getAddedEdges().values()) {
                if (!isIdAlreadyTaken(edge.getPointedSynset())) {
                    Synset pointedSynset = update.getPointedSynset(edge);
                    attachedDatabase.addSynset(pointedSynset);
                    synsets.put(pointedSynset.getId(), pointedSynset);
                }
                attachedDatabase.addEdge(edge);
            }

            for (EdgeEdit edgeEdit : update.getEdgeEdits().values()) {
                attachedDatabase.editEdge(edgeEdit.getEdited(), edgeEdit.getOriginal());
            }

            // If synset is already loaded on board replace it with the updated version.
            if (synsets.containsKey(update.getOriginalSynset().getId())) {
                synsets.put(update.getOriginalSynset().getId(), update.getUpdatedSynset());
            }
        }
    }

    public String export(RDFFormat format) {
        Model model = attachedDatabase.getAllStatements();
        StringWriter buffer = new StringWriter();
        Rio.write(model, buffer, format);
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

    public boolean isBoardEdited() {
        return !removedSynsets.isEmpty() || !newSynsets.isEmpty() || !synsetEdits.isEmpty();
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

    public Map<String,Synset> getSynsets() {
        return synsets;
    }
}