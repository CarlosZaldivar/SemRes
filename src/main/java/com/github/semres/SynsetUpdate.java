package com.github.semres;

import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetSynset;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SynsetUpdate {
    private final BabelNetSynset originalSynset;
    private BabelNetSynset updatedSynset;
    private final Map<String, Edge> addedEdges = new HashMap<>();
    private final Map<String, Edge> removedEdges = new HashMap<>();
    private final Map<String, EdgeEdit> edgeEdits = new HashMap<>();
    private final Map<String, BabelNetSynset> relatedSynsets;
    private boolean isSynsetDataUpdated = false;

    public SynsetUpdate(BabelNetSynset originalSynset, BabelNetSynset updatedSynset, Map<String, BabelNetSynset> relatedSynsets) {
        this.originalSynset = originalSynset;
        this.relatedSynsets = relatedSynsets;
        if (updatedSynset == null) {
            this.updatedSynset = null;
            return;
        }

        isSynsetDataUpdated = synsetsAreDifferent(originalSynset, updatedSynset);

        if (originalSynset.isExpanded() && updatedSynset.isExpanded()) {
            Map<String, Edge> originalEdges = originalSynset.getOutgoingEdges();
            Map<String, Edge> updatedEdges = updatedSynset.getOutgoingEdges();

            // Remove from the updated synset edges that were previously removed by user.
            for (String removedSynsetRelation : originalSynset.getRemovedRelations()) {
                String edgeId = originalSynset.getId() + "-" + removedSynsetRelation;
                if (updatedEdges.containsKey(edgeId)) {
                    updatedSynset = updatedSynset.removeOutgoingEdge(edgeId);
                    updatedEdges.remove(edgeId);
                }
            }

            for (Edge edge : originalEdges.values().stream().filter((e) -> e instanceof BabelNetEdge).collect(Collectors.toList())) {
                if (!updatedEdges.containsKey(edge.getId())) {
                    removedEdges.put(edge.getId(), edge);
                } else if (edgesAreDifferent(edge, updatedEdges.get(edge.getId()))) {
                    edgeEdits.put(edge.getId(), new EdgeEdit(edge, updatedEdges.get(edge.getId())));
                }

                // Remove corresponding edge from the other map to avoid iterating over it in the next step.
                updatedEdges.remove(edge.getId());
            }

            for (Edge edge : updatedEdges.values()) {
                if (!originalEdges.containsKey(edge.getId())) {
                    addedEdges.put(edge.getId(), edge);
                }
            }
        }
        this.updatedSynset = updatedSynset;
    }

    private boolean synsetsAreDifferent(BabelNetSynset originalSynset, BabelNetSynset updatedSynset) {
        return !originalSynset.getRepresentation().equals(updatedSynset.getRepresentation()) ||
               !Objects.equals(originalSynset.getDescription(), updatedSynset.getDescription());
    }

    private boolean edgesAreDifferent(Edge originalEdge, Edge updatedEdge) {
        return originalEdge.getRelationType() != updatedEdge.getRelationType() || originalEdge.getWeight() != updatedEdge.getWeight() ||
               !Objects.equals(originalEdge.getDescription(), updatedEdge.getDescription());
    }

    public BabelNetSynset getOriginalSynset() {
        return originalSynset;
    }

    public BabelNetSynset getUpdatedSynset() {
        return updatedSynset;
    }

    public boolean isSynsetDataUpdated() {
        return isSynsetDataUpdated;
    }

    public boolean isSynsetUpdated() {
        return isSynsetDataUpdated || !addedEdges.isEmpty() || !removedEdges.isEmpty() || !edgeEdits.isEmpty();
    }

    public Map<String, Edge> getAddedEdges() {
        return new HashMap<>(addedEdges);
    }

    public Map<String, Edge> getRemovedEdges() {
        return new HashMap<>(removedEdges);
    }

    public Map<String, EdgeEdit> getEdgeEdits() {
        return new HashMap<>(edgeEdits);
    }

    public BabelNetSynset getPointedSynset(Edge edge) {
        return relatedSynsets.get(edge.getPointedSynset());
    }

    public void cancelEdgeRemoval(String id) {
        updatedSynset = updatedSynset.addOutgoingEdge(originalSynset.getOutgoingEdges().get(id));
        removedEdges.remove(id);
    }

    public void cancelEdgeAddition(String id) {
        updatedSynset = updatedSynset.removeOutgoingEdge(id);
        // Since removing edge updates removedRelations set, we also want to update the synset itself
        isSynsetDataUpdated = true;
        addedEdges.remove(id);
    }

    public void cancelEdgeEdition(String id) {
        edgeEdits.remove(id);
    }

    public void cancelSynsetEdition() {
        isSynsetDataUpdated = false;
    }
}
