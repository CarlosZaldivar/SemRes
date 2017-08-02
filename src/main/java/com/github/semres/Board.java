package com.github.semres;

import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.babelnet.CommonIRI;
import com.github.semres.gui.IDAlreadyTakenException;
import com.github.semres.user.UserEdge;
import com.github.semres.user.UserSynset;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;
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
        if (synset.hasDatabaseEdgesLoaded()) {
            throw new RuntimeException("Edges already loaded");
        }

        Collection<Edge> edgesAlreadyPresent = synset.getOutgoingEdges().values();
        attachedDatabase.loadEdges(synset);

        for (Edge edge : synset.getOutgoingEdges().values()) {
            if (!synsets.containsKey(edge.getPointedSynsetId())) {
                Synset pointedSynset = attachedDatabase.getSynset(edge.getPointedSynsetId());
                synsets.put(pointedSynset.getId(), pointedSynset);
            }
        }

        return new ArrayList<>(CollectionUtils.disjunction(edgesAlreadyPresent, synset.getOutgoingEdges().values()));
    }

    public List<Edge> downloadBabelNetEdges(String synsetId) throws IOException {
        BabelNetSynset updatedSynset = (BabelNetSynset) synsets.get(synsetId);
        babelNetManager.loadEdges(updatedSynset);

        SynsetEdit synsetEdit;
        if (!synsetEdits.containsKey(synsetId)) {
            synsetEdit = new SynsetEdit(synsets.get(synsetId), updatedSynset);
            synsetEdits.put(synsetId, synsetEdit);
        } else {
            synsetEdit = synsetEdits.get(synsetId);
            synsetEdit.setEdited(updatedSynset);
        }

        // Get BabelNetEdges
        List<Edge> edges = updatedSynset.getOutgoingEdges().values().stream()
                .filter((edge) -> edge instanceof BabelNetEdge)
                .collect(Collectors.toList());

        for (Edge edge : edges) {
            synsetEdit.addEdge(edge);
            String pointedSynsetId = edge.getPointedSynsetId();

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

    public UserSynset createSynset(String representation) {
        return createSynset(representation, null);
    }

    public UserSynset createSynset(String representation, String description) {
        String id = attachedDatabase.generateNewSynsetId();
        UserSynset newSynset = new UserSynset(representation, id, description);
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
        return newSynset;
    }

    public void addSynset(Synset newSynset) {
        if (synsetExists(newSynset.getId())) {
            throw new IDAlreadyTakenException();
        }
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
    }

    public void addEdge(Edge newEdge) {
        if (edgeAlreadyExists(newEdge.getId())) {
            throw new IDAlreadyTakenException();
        }

        String originSynsetId = newEdge.getOriginSynsetId();
        String pointedSynsetId = newEdge.getPointedSynsetId();

        if (synsets.get(originSynsetId) == null) {
            throw new RuntimeException("Trying to add edge without corresponding origin synset on the board.");
        }
        if (synsets.get(pointedSynsetId) == null) {
            throw new RuntimeException("Trying to add edge without corresponding pointed synset on the board.");
        }


        Synset originalSynset = synsets.get(originSynsetId);
        Synset editedSynset = originalSynset.addOutgoingEdge(newEdge);

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
    public void editSynset(UserSynset originalSynset, UserSynset editedSynset) {
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
        String originSynsetId = oldEdge.getOriginSynsetId();
        if (!synsetEdits.containsKey(originSynsetId)) {
            synsetEdit = new SynsetEdit(synsets.get(originSynsetId), synsets.get(originSynsetId));
            synsetEdits.put(originSynsetId, synsetEdit);
        } else {
            synsetEdit = synsetEdits.get(originSynsetId);
        }

        synsetEdit.editEdge(oldEdge, editedEdge);

        Synset synset = synsets.get(originSynsetId);
        synset = synset.changeOutgoingEdge(editedEdge);
        synsets.put(originSynsetId, synset);
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
        if (isEdited()) {
            throw new RuntimeException("Cannot check for updates with unsaved changes.");
        }

        List<Synset> synsets = attachedDatabase.getSynsets(CommonIRI.BABELNET_SYNSET);
        List<SynsetUpdate> updates = new ArrayList<>();
        for (Synset synset : synsets) {
            SynsetUpdate update = parseUpdates((BabelNetSynset) synset);
            if (update != null) {
                updates.add(update);
            }
        }
        return updates;
    }

    public SynsetUpdate checkForUpdates(String checkedSynsetId) throws IOException {
        if (isEdited()) {
            throw new RuntimeException("Cannot check for updates with unsaved changes.");
        }

        Synset synset = attachedDatabase.getSynset(checkedSynsetId);
        return parseUpdates((BabelNetSynset) synset);
    }

    private SynsetUpdate parseUpdates(BabelNetSynset originalSynset) throws IOException {
        if (!originalSynset.hasDatabaseEdgesLoaded()) {
            attachedDatabase.loadEdges(originalSynset);
        }

        BabelNetSynset updatedSynset = babelNetManager.getSynset(originalSynset.getId());

        // If synset is not found in BabelNet, remove it.
        if (updatedSynset == null) {
            return new SynsetUpdate(originalSynset, null, null);
        }

        SynsetUpdate synsetUpdate;
        try {
            if (!originalSynset.isDownloadedWithEdges()) {
                synsetUpdate = new SynsetUpdate(originalSynset, updatedSynset, new HashMap<>());
            } else {
                babelNetManager.loadEdges(updatedSynset);
                synsetUpdate = new SynsetUpdate(originalSynset, updatedSynset, getRelatedSynsets(originalSynset, updatedSynset));
            }
        } catch (SynsetNotUpdatedException e) {
            return null;
        }
        return synsetUpdate;
    }

    private HashMap<String, BabelNetSynset> getRelatedSynsets(BabelNetSynset originalSynset, BabelNetSynset updatedSynset) throws IOException {
        HashMap<String, BabelNetSynset> relatedSynsets = new HashMap<>();
        for (Edge edge : originalSynset.getOutgoingEdges().values().stream().filter((e) -> e instanceof BabelNetEdge).collect(Collectors.toList())) {
            relatedSynsets.put(edge.getPointedSynsetId(), (BabelNetSynset) loadSynset(edge.getPointedSynsetId()));
        }

        for (Edge edge : updatedSynset.getOutgoingEdges().values()) {
            if (synsetExists(edge.getPointedSynsetId())) {
                relatedSynsets.put(edge.getPointedSynsetId(), (BabelNetSynset) loadSynset(edge.getPointedSynsetId()));
            } else {
                relatedSynsets.put(edge.getPointedSynsetId(), babelNetManager.getSynset(edge.getPointedSynsetId()));
            }
        }
        return relatedSynsets;
    }

    public void update(List<SynsetUpdate> updates) {
        if (isEdited()) {
            throw new RuntimeException("Cannot update with unsaved changes.");
        }

        for (SynsetUpdate update : updates) {
            if (update.getUpdatedSynset() == null) {
                attachedDatabase.removeSynset(update.getOriginalSynset());
                synsets.remove(update.getOriginalSynset().getId());
                continue;
            }

            if (update.areSynsetPropertiesUpdated()) {
                attachedDatabase.editSynset(update.getUpdatedSynset(), update.getOriginalSynset());
            }

            for (Edge edge : update.getRemovedEdges().values()) {
                attachedDatabase.removeEdge(edge);
            }

            for (Edge edge : update.getAddedEdges().values()) {
                if (!synsetExists(edge.getPointedSynsetId())) {
                    Synset pointedSynset = update.getPointedSynset(edge);
                    attachedDatabase.addSynset(pointedSynset);
                    synsets.put(pointedSynset.getId(), pointedSynset);
                }
                attachedDatabase.addEdge(edge);
            }

            for (EdgeEdit edgeEdit : update.getEdgeEdits().values()) {
                attachedDatabase.editEdge(edgeEdit.getEdited(), edgeEdit.getOriginal());
            }

            for (EdgeEdit edgeEdit : update.getEdgesToMerge().values()) {
                attachedDatabase.editEdge(edgeEdit.getEdited(), edgeEdit.getOriginal());
            }

            // If synset is already loaded on board replace it with the updated version.
            if (synsets.containsKey(update.getOriginalSynset().getId())) {
                synsets.put(update.getOriginalSynset().getId(), update.getUpdatedSynset());
            }
        }
    }

    public String export(RDFFormat format) {
        return attachedDatabase.export(format);
    }

    public boolean synsetExists(String id) {
        if (synsets.containsKey(id)) {
            return true;
        }
        if (attachedDatabase.hasSynset(id)) {
            return true;
        }
        return false;
    }

    public boolean isEdited() {
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

    Map<String,Synset> getSynsets() {
        return synsets;
    }

    public List<RelationType> getRelationTypes() {
        return attachedDatabase.getRelationTypes();
    }

    public void addRelationType(RelationType relationType) {
        attachedDatabase.addRelationType(relationType);
    }

    public void removeRelationType(RelationType relationType) {
        if (relationTypeInUse(relationType)) {
            throw new RelationTypeInUseException();
        }
        attachedDatabase.removeRelationType(relationType);
    }

    private boolean relationTypeInUse(RelationType relationType) {
        if (attachedDatabase.relationTypeInUse(relationType)) {
            return true;
        }

        // Check if a new or edited edge was added with the specified type
        for (SynsetEdit synsetEdit : synsetEdits.values()) {
            for (Edge edge : synsetEdit.getAddedEdges().values()) {
                if (edge.getRelationType().equals(relationType)) {
                    return true;
                }
            }

            for (EdgeEdit edgeEdit : synsetEdit.getEdgeEdits().values()) {
                if (edgeEdit.getEdited().getRelationType().equals(relationType)) {
                    return true;
                }
            }
        }
        return false;
    }
}