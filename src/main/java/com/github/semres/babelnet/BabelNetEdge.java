package com.github.semres.babelnet;

import com.github.semres.Synset;
import com.github.semres.Edge;

public class BabelNetEdge extends Edge {
    public BabelNetEdge(Synset toSynset, Synset originSynset, RelationType relationType, double weight) {
        super(toSynset, originSynset, relationType, weight);
    }

    public BabelNetEdge(Synset pointedSynset, Synset originSynset, RelationType relationType, String description, double weight) {
        super(pointedSynset, originSynset, relationType, weight);
        this.description = description;
    }
}
