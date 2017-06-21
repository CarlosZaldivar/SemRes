package com.github.semres.babelnet;

import com.github.semres.EdgeSerializer;
import com.github.semres.RelationType;
import com.github.semres.SemRes;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BabelNetEdgeSerializer extends EdgeSerializer {
    public BabelNetEdgeSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }

    @Override
    public BabelNetEdge rdfToEdge(IRI edgeIri) {
        String queryString = String.format("SELECT ?originSynsetId ?pointedSynsetId ?weight ?relationTypeName ?relationTypeSource ?description ?lastEdited " +
                "WHERE { ?originSynset <%1$s> ?pointedSynset . <%1$s> <%2$s> ?weight . " +
                "?originSynset <%3$s> ?originSynsetId . ?pointedSynset <%3$s> ?pointedSynsetId . " +
                "<%1$s> <%4$s> ?relationType . ?relationType <%5$s> ?relationTypeName . ?relationType <%6$s> ?relationTypeSource . " +
                "OPTIONAL { <%1$s> <%7$s> ?description } . OPTIONAL { <%1$s> <%8$s> ?lastEdited }}",
                edgeIri.stringValue(), SemRes.WEIGHT, SemRes.ID, SemRes.RELATION_TYPE_PROPERTY, RDFS.LABEL, SemRes.SOURCE, RDFS.COMMENT, SemRes.LAST_EDITED);
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

        RelationType relationType = new RelationType(result.getValue("relationTypeName").stringValue(),
                                                     result.getValue("relationTypeSource").stringValue());


        String description = null;
        if (result.hasBinding("description")) {
            description = result.getValue("description").stringValue();
        }

        double weight = Double.parseDouble(result.getValue("weight").stringValue());

        BabelNetEdge edge = new BabelNetEdge(pointedSynset, originSynset, description, relationType, weight);

        if (result.hasBinding("lastEdited")) {
            edge.setLastEditedTime(LocalDateTime.parse(result.getValue("lastEdited").stringValue(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
        }
        return edge;
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
