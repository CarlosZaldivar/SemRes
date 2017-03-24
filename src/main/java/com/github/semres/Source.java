package com.github.semres;

import org.eclipse.rdf4j.model.Model;

public abstract class Source {
    public abstract Class<? extends SynsetSerializer> getSynsetSerializerClass();
    public abstract Class<? extends EdgeSerializer> getEdgeSerializerClass();
    public abstract Model getMetadataStatements();
}
