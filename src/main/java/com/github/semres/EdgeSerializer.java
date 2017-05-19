package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;

public abstract class EdgeSerializer {
    protected final Repository repository;
    protected final String baseIri;

    protected EdgeSerializer(Repository repository, String baseIri) {
        this.repository = repository;
        this.baseIri = baseIri;
    }

    public Model edgeToRdf(Edge edge) {
        Model model = new LinkedHashModel();
        ValueFactory factory = repository.getValueFactory();

        IRI edgeIri = factory.createIRI(baseIri + "outgoingEdges/" + edge.getId());

        if (edge.getDescription() != null) {
            Literal description = factory.createLiteral(edge.getDescription());
            model.add(factory.createStatement(edgeIri, RDFS.COMMENT, description));
        }

        model.add(edgeIri, RDF.TYPE, getEdgeClassIri());
        model.add(edgeIri, SemRes.ID, factory.createLiteral(edge.getId()));

        switch (edge.getRelationType()) {
            case HOLONYM:
                model.add(edgeIri, SemRes.RELATION_TYPE, SemRes.HOLONYM);
                break;
            case HYPERNYM:
                model.add(edgeIri, SemRes.RELATION_TYPE, SemRes.HYPERNYM);
                break;
            case HYPONYM:
                model.add(edgeIri, SemRes.RELATION_TYPE, SemRes.HYPONYM);
                break;
            case MERONYM:
                model.add(edgeIri, SemRes.RELATION_TYPE, SemRes.MERONYM);
                break;
        }

        model.add(factory.createIRI(baseIri + "synsets/" + edge.getOriginSynset()), edgeIri, factory.createIRI(baseIri + "synsets/" + edge.getPointedSynset()));
        model.add(edgeIri, SemRes.WEIGHT, factory.createLiteral(edge.getWeight()));

        return model;
    }

    abstract public Edge rdfToEdge(String edgeId);
    abstract public Edge rdfToEdge(IRI edge);
    abstract public String getEdgeClass();
    abstract public IRI getEdgeClassIri();

    protected Edge.RelationType relationIriToEnum(IRI relationIri) {
        if (relationIri.stringValue().equals(SemRes.HOLONYM.stringValue())) {
            return Edge.RelationType.HOLONYM;
        } if (relationIri.stringValue().equals(SemRes.HYPERNYM.stringValue())) {
            return Edge.RelationType.HYPERNYM;
        } if (relationIri.stringValue().equals(SemRes.HYPONYM.stringValue())) {
            return Edge.RelationType.HYPONYM;
        } else if (relationIri.stringValue().equals(SemRes.MERONYM.stringValue())) {
            return Edge.RelationType.MERONYM;
        } else {
            return Edge.RelationType.OTHER;
        }
    }
}
