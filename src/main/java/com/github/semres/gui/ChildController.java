package com.github.semres.gui;

abstract class ChildController extends Controller {
    Controller parent;

    public void setParent(Controller parent) {
        this.parent = parent;
    }
}
