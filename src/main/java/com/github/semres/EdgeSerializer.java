package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
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
    abstract public Edge rdfToEdge(String edgeId, Synset pointedSynset, Synset originSynset);
    abstract public Edge rdfToEdge(IRI edge, Synset pointedSynset, Synset originSynset);
    abstract public String getEdgeClass();
    abstract public IRI getEdgeClassIri();

    protected Edge.RelationType relationIriToEnum(IRI relationIri) {
        if (relationIri.stringValue().equals(SR.HOLONYM.stringValue())) {
            return Edge.RelationType.HOLONYM;
        } if (relationIri.stringValue().equals(SR.HYPERNYM.stringValue())) {
            return Edge.RelationType.HYPERNYM;
        } if (relationIri.stringValue().equals(SR.HYPONYM.stringValue())) {
            return Edge.RelationType.HYPONYM;
        } else if (relationIri.stringValue().equals(SR.MERONYM.stringValue())) {
            return Edge.RelationType.MERONYM;
        } else {
            return Edge.RelationType.OTHER;
        }
    }
}
