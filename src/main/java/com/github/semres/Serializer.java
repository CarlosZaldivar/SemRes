package com.github.semres;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;

abstract public class Serializer {
    protected Repository repository;
    protected String baseIri;

    public Serializer(Repository repository, String baseIri) {
        this.repository = repository;
        this.baseIri = baseIri;
    }

    abstract public Model synsetToRdf(Synset synset);
    abstract public Synset rdfToSynset(String synsetId);
}
