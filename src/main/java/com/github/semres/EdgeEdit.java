package com.github.semres;

class EdgeEdit {
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

    Edge getOriginal() {
        return original;
    }

    Edge getEdited() {
        return edited;
    }

    String getOriginSynset() {
        return original.getOriginSynset();
    }

    String getPointedSynset() {
        return original.getPointedSynset();
    }
}
