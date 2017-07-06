package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.RelationType;

import java.time.LocalDateTime;

public class BabelNetEdge extends Edge {
    public BabelNetEdge(String pointedSynset, String originSynset, RelationType relationType, double weight) {
        super(pointedSynset, originSynset, relationType, weight);
    }

    public BabelNetEdge(String pointedSynset, String originSynset, String description, RelationType relationType, double weight) {
        super(pointedSynset, originSynset, description, relationType, weight);
    }

    public BabelNetEdge(String pointedSynset, String originSynset, String description, RelationType relationType, double weight, LocalDateTime lastEditedTime) {
        super(pointedSynset, originSynset, description, relationType, weight, lastEditedTime);
    }
}
