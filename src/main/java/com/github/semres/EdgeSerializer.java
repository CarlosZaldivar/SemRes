package com.github.semres;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;

public abstract class EdgeSerializer {
    protected Repository repository;
    protected String baseIri;

    public EdgeSerializer(Repository repository, String baseIri) {
        this.repository = repository;
        this.baseIri = baseIri;
    }

    abstract public Model edgeToRdf(Edge edge);
    abstract public Edge rdfToEdge(String edgeId);
}
