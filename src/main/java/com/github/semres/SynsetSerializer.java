package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;

public abstract class SynsetSerializer {
    protected final String baseIri;

    public SynsetSerializer(String baseIri) {
        this.baseIri = baseIri;
    }

    public abstract Model synsetToRdf(Synset synset);
    public abstract Synset rdfToSynset(String synsetId, Repository repository);
    public abstract Synset rdfToSynset(IRI synsetIri, Repository repository);
    public abstract String getSynsetClass();
    public abstract IRI getSynsetClassIri();
}
