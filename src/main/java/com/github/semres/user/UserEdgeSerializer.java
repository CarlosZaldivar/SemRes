package com.github.semres.user;

import com.github.semres.Edge;
import com.github.semres.EdgeSerializer;
import com.github.semres.SR;
import com.github.semres.Synset;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.util.List;

public class UserEdgeSerializer extends EdgeSerializer {
    public UserEdgeSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }

    @Override
    public Model edgeToRdf(Edge edge) {
        if (!(edge instanceof UserEdge)) {
            throw new IllegalArgumentException();
        }

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

    @Override
    public String getEdgeClass() {
        return "com.github.semres.user.UserEdge";
    }

    @Override
    public IRI getEdgeClassIri() {
        return repository.getValueFactory().createIRI(baseIri + "classes/UserEdge");
    }

    @Override
    public UserEdge rdfToEdge(IRI edgeIri, Synset pointedSynset, Synset originSynset) {
        ValueFactory factory = repository.getValueFactory();
        String description = null;
        Edge.RelationType relationType = null;
        double weight = -1;

        String queryString = String.format("SELECT * WHERE { <%s> ?p ?o }", edgeIri.stringValue());
        List<BindingSet> results = Repositories.tupleQuery(repository, queryString, r -> QueryResults.asList(r));

        for (BindingSet result: results) {
            if (result.getValue("p").stringValue().equals(SR.RELATION_TYPE.stringValue())) {
                IRI relationIri = factory.createIRI(result.getValue("o").stringValue());
                relationType = relationIriToEnum(relationIri);
            } else if (result.getValue("p").stringValue().equals(RDFS.COMMENT.stringValue())) {
                description = result.getValue("o").stringValue();
            } else if (result.getValue("p").stringValue().equals(SR.WEIGHT.stringValue())) {
                weight = Double.parseDouble(result.getValue("o").stringValue());
            }
        }

        if (relationType == null) {
            relationType = Edge.RelationType.OTHER;
        }

        UserEdge edge = new UserEdge(pointedSynset, originSynset, relationType, weight);

        if (description != null) {
            edge.setDescription(description);
        }

        return edge;
    }

    @Override
    public UserEdge rdfToEdge(String edgeId, Synset pointedSynset, Synset originSynset) {
        ValueFactory factory = repository.getValueFactory();
        return rdfToEdge(factory.createIRI(baseIri + "outgoingEdges/" + edgeId), pointedSynset, originSynset);
    }
}
