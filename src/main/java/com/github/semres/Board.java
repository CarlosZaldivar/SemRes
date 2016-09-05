package com.github.semres;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private Map<String, Synset> synsets = new HashMap<>();
    private Map<String, Boolean> synsetsLoadedState = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();

    private Database attachedDatabase;

    public Board(Database attachedDatabase) {
        this.attachedDatabase = attachedDatabase;
    }
}
