package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;

import java.time.ZoneId;
import java.util.Date;

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

        model.add(edgeIri, SemRes.RELATION_TYPE_PROPERTY, factory.createIRI(baseIri + "relationTypes/" + edge.getRelationType().getType()));

        model.add(factory.createIRI(baseIri + "synsets/" + edge.getOriginSynsetId()), edgeIri, factory.createIRI(baseIri + "synsets/" + edge.getPointedSynsetId()));
        model.add(edgeIri, SemRes.WEIGHT, factory.createLiteral(edge.getWeight()));

        if (edge.getLastEditedTime() != null) {
            model.add(edgeIri, SemRes.LAST_EDITED, factory.createLiteral(Date.from(edge.getLastEditedTime().atZone(ZoneId.systemDefault()).toInstant())));
        }

        return model;
    }

    abstract public Edge rdfToEdge(String edgeId);
    abstract public Edge rdfToEdge(IRI edge);
    abstract public String getEdgeClass();
    abstract public IRI getEdgeClassIri();
}
