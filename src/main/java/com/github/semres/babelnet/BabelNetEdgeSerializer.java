package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.EdgeSerializer;
import com.github.semres.SR;
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
    public BabelNetEdge rdfToEdge(IRI edgeIri) {
        ValueFactory factory = repository.getValueFactory();

        String queryString = String.format("SELECT ?originSynsetId ?pointedSynsetId ?weight ?relationType ?description " +
                "WHERE { ?originSynset <%1$s> ?pointedSynset . <%1$s> <%2$s> ?weight . " +
                "?originSynset <%5$s> ?originSynsetId . ?pointedSynset <%5$s> ?pointedSynsetId . " +
                "OPTIONAL { <%1$s> <%3$s> ?description } . OPTIONAL { <%1$s> <%4$s> ?relationType} }",
                edgeIri.stringValue(), SR.WEIGHT, RDFS.COMMENT, SR.RELATION_TYPE, SR.ID);
        List<BindingSet> results = Repositories.tupleQuery(repository, queryString, iter -> QueryResults.asList(iter));

        if (results.size() > 1) {
            throw new RuntimeException("More than one directed edge between synsets saved in the database.");
        }
        if (results.size() != 1) {
            throw new RuntimeException("Could not find edge with specified IRI in the database.");
        }

        BindingSet result = results.get(0);

        String originSynset = result.getValue("originSynsetId").stringValue();
        String pointedSynset = result.getValue("pointedSynsetId").stringValue();

        Edge.RelationType relationType;
        if (result.hasBinding("relationType")) {
            relationType = relationIriToEnum(factory.createIRI(result.getValue("relationType").stringValue()));
        } else {
            relationType = Edge.RelationType.OTHER;
        }

        String description = null;
        if (result.hasBinding("description")) {
            description = result.getValue("description").stringValue();
        }

        double weight = Double.parseDouble(result.getValue("weight").stringValue());

        if (relationType == null) {
            relationType = Edge.RelationType.OTHER;
        }

        return new BabelNetEdge(pointedSynset, originSynset, description, relationType, weight);
    }

    @Override
    public BabelNetEdge rdfToEdge(String edgeId) {
        ValueFactory factory = repository.getValueFactory();
        return rdfToEdge(factory.createIRI(baseIri + "outgoingEdges/" + edgeId));
    }

    @Override
    public String getEdgeClass() {
        return "com.github.semres.babelnet.BabelNetEdge";
    }

    @Override
    public IRI getEdgeClassIri() {
        return CommonIRI.BABELNET_EDGE;
    }
}
