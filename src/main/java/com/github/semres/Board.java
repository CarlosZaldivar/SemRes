package com.github.semres;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Board {
    public Map<String, Synset> getSynsets() {
        return synsets;
    }

    private Map<String, Synset> synsets = new HashMap<>();
    private Map<String, Boolean> synsetsLoadedState = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();

    private Map<String, Synset> newSynsets = new HashMap<>();

    private Database attachedDatabase;

    public Board(Database attachedDatabase) {
        this.attachedDatabase = attachedDatabase;
        for (Synset synset : this.attachedDatabase.getSynsets()) {
            synsets.put(synset.getId(), synset);
        }
    }

    public void addSynset(Synset newSynset) {
        if (newSynset.getId() == null) {
            newSynset.setId(getNewSynsetId());
        }
        synsets.put(newSynset.getId(), newSynset);
        newSynsets.put(newSynset.getId(), newSynset);
    }

    public void save() {
        for (Synset synset : newSynsets.values()) {
            attachedDatabase.addSynset(synset);
        }
        newSynsets.clear();
    }

    /**
     * Generate unique synset id.
     * @return Unique synset id.
     */
    private String getNewSynsetId() {
        SecureRandom random = new SecureRandom();
        String id;
        while (true) {
            id = new BigInteger(130, random).toString(32);
            if (!synsets.containsKey(id)) {
                break;
            }
        }
        return id;
    }
}
