package com.github.semres;

public abstract class Source {
    public abstract Class<? extends SynsetSerializer> getSynsetSerializerClass();
    public abstract Class<? extends EdgeSerializer> getEdgeSerializerClass();
    public abstract Class<? extends Synset> getSynsetClass();
}
