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
    protected Repository repository;
    protected String baseIri;

    public EdgeSerializer(Repository repository, String baseIri) {
        this.repository = repository;
        this.baseIri = baseIri;
    }

    public Model edgeToRdf(Edge edge) {
        Model model = new LinkedHashModel();
        ValueFactory factory = repository.getValueFactory();

        model.add(getEdgeClassIri(), RDF.TYPE, RDFS.CLASS);
        model.add(getEdgeClassIri(), RDFS.SUBCLASSOF, SR.EDGE);

        IRI edgeIri = factory.createIRI(baseIri + "outgoingEdges/" + edge.getId());

        if (edge.getDescription() != null) {
            Literal description = factory.createLiteral(edge.getDescription());
            model.add(factory.createStatement(edgeIri, RDFS.COMMENT, description));
        }

        model.add(edgeIri, RDF.TYPE, getEdgeClassIri());
        model.add(edgeIri, SR.ID, factory.createLiteral(edge.getId()));

        switch (edge.getRelationType()) {
            case HOLONYM:
                model.add(edgeIri, SR.RELATION_TYPE, SR.HOLONYM);
                break;
            case HYPERNYM:
                model.add(edgeIri, SR.RELATION_TYPE, SR.HYPERNYM);
                break;
            case HYPONYM:
                model.add(edgeIri, SR.RELATION_TYPE, SR.HYPONYM);
                break;
            case MERONYM:
                model.add(edgeIri, SR.RELATION_TYPE, SR.MERONYM);
                break;
        }

        model.add(factory.createIRI(baseIri + "synsets/" + edge.getOriginSynset().getId()), edgeIri, factory.createIRI(baseIri + "synsets/" + edge.getPointedSynset().getId()));
        model.add(edgeIri, SR.WEIGHT, factory.createLiteral(edge.getWeight()));

        return model;
    }

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
