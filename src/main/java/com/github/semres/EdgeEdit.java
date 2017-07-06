package com.github.semres;

public class EdgeEdit {
    private final Edge original;
    private Edge edited;

    EdgeEdit(Edge original, Edge edited) {
        if (!original.getId().equals(edited.getId())) {
            throw new IllegalArgumentException("Edges between different synsets.");
        }
        this.original = original;
        this.edited = edited;
    }

    void setEdited(Edge edited) {
        this.edited = edited;
    }

    public Edge getOriginal() {
        return original;
    }

    public Edge getEdited() {
        return edited;
    }

    public String getOriginSynset() {
        return original.getOriginSynsetId();
    }

    public String getPointedSynset() {
        return original.getPointedSynsetId();
    }

    public String getId() {
        return original.getId();
    }
}
