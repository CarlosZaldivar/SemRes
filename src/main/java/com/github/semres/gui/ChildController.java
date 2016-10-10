package com.github.semres.gui;

public abstract class ChildController extends Controller {
    protected Controller parent;

    public void setParent(Controller parent) {
        this.parent = parent;
    }
}
