package com.github.semres;

import org.eclipse.rdf4j.model.Model;

import java.util.Collection;

public abstract class Source {
    public abstract Class<? extends SynsetSerializer> getSynsetSerializerClass();
    public abstract Class<? extends EdgeSerializer> getEdgeSerializerClass();
    public abstract Model getMetadataStatements();
    public abstract Collection<RelationType> getRelationTypes();
}
