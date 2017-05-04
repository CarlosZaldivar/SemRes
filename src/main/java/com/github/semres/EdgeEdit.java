package com.github.semres;

class EdgeEdit {
    private final Edge original;
    private Edge edited;

    EdgeEdit(Edge original, Edge edited) {
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
}
