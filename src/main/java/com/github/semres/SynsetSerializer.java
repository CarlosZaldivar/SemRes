package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;

public abstract class SynsetSerializer {
    protected Repository repository;
    protected String baseIri;

    public SynsetSerializer(Repository repository, String baseIri) {
        this.repository = repository;
        this.baseIri = baseIri;
    }

    public abstract Model synsetToRdf(Synset synset);
    public abstract Synset rdfToSynset(String synsetId);
    public abstract Synset rdfToSynset(IRI synsetIri);
    public abstract String getSynsetClass();
    public abstract IRI getSynsetClassIri();
}
