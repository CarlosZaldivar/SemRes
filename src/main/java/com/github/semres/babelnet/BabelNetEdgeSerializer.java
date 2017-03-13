package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.EdgeSerializer;
import com.github.semres.SR;
import com.github.semres.Synset;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.util.List;

public class BabelNetEdgeSerializer extends EdgeSerializer {
    public BabelNetEdgeSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }

    @Override
    public String getEdgeClass() {
        return "com.github.semres.babelnet.BabelNetEdge";
    }

    @Override
    public IRI getEdgeClassIri() {
        return repository.getValueFactory().createIRI(baseIri + "classes/BabelNetEdge");
    }

    @Override
    public BabelNetEdge rdfToEdge(IRI edgeIri, Synset pointedSynset, Synset originSynset) {
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

        BabelNetEdge edge;

        if (description != null) {
            edge = new BabelNetEdge(pointedSynset, originSynset, relationType, description, weight);
        } else {
            edge = new BabelNetEdge(pointedSynset, originSynset, relationType, weight);
        }

        return edge;
    }

    @Override
    public BabelNetEdge rdfToEdge(String edgeId, Synset pointedSynset, Synset originSynset) {
        ValueFactory factory = repository.getValueFactory();
        return rdfToEdge(factory.createIRI(baseIri + "outgoingEdges/" + edgeId), pointedSynset, originSynset);
    }
}
