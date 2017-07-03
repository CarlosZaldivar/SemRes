package com.github.semres.babelnet;

import com.github.semres.SemRes;
import com.github.semres.SynsetSerializer;
import com.github.semres.Synset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.memory.model.BooleanMemLiteral;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BabelNetSynsetSerializer extends SynsetSerializer {
    public BabelNetSynsetSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }

    @Override
    public Model synsetToRdf(Synset synset) {
        if (!(synset instanceof BabelNetSynset)) {
            throw new IllegalArgumentException();
        }

        Model model = new LinkedHashModel();
        ValueFactory factory = repository.getValueFactory();

        IRI synsetIri = factory.createIRI(baseIri + "synsets/" + synset.getId());

        if (synset.getRepresentation() != null) {
            Literal representation = factory.createLiteral(synset.getRepresentation());
            model.add(factory.createStatement(synsetIri, RDFS.LABEL, representation));
        }

        if (synset.getDescription() != null) {
            Literal description = factory.createLiteral(synset.getDescription());
            model.add(factory.createStatement(synsetIri, RDFS.COMMENT, description));
        }

        if (synset.getLastEditedTime() != null) {
            model.add(synsetIri, SemRes.LAST_EDITED, factory.createLiteral(Date.from(synset.getLastEditedTime().atZone(ZoneId.systemDefault()).toInstant())));
        }

        model.add(synsetIri, RDF.TYPE, getSynsetClassIri());
        model.add(synsetIri, SemRes.ID, factory.createLiteral(synset.getId()));

        for (String removedRelation : ((BabelNetSynset) synset).getRemovedRelations()) {
            IRI removedRelationIRI = factory.createIRI(baseIri + "synsets/" + synset.getId() + "/removedRelations/" + removedRelation);
            model.add(factory.createStatement(removedRelationIRI, SemRes.ID, factory.createLiteral(removedRelation)));
            model.add(factory.createStatement(synsetIri, SemRes.REMOVED_RELATION, removedRelationIRI));
        }

        // Add information if BabelNet edges has been downloaded.
        model.add(synsetIri, CommonIRI.EDGES_DOWNLOADED, factory.createLiteral(((BabelNetSynset) synset).isDownloadedWithEdges()));

        return model;
    }

    @Override
    public BabelNetSynset rdfToSynset(String synsetId) {
        ValueFactory factory = repository.getValueFactory();
        return rdfToSynset(factory.createIRI(baseIri + "synsets/" + synsetId));
    }

    @Override
    public BabelNetSynset rdfToSynset(IRI synsetIri) {
        BabelNetSynset synset;
        boolean edgesLoaded;

        try (RepositoryConnection conn = repository.getConnection()) {

            // Get synset representation, id, description and the flag if the edges from BabelNet were loaded or not
            String queryString = String.format("SELECT ?id ?representation ?edgesLoaded ?description ?lastEdited " +
                            "WHERE { <%1$s> <%2$s> ?id . <%1$s> <%3$s> ?representation . <%1$s> <%4$s> ?edgesLoaded . " +
                            "OPTIONAL { <%1$s> <%5$s> ?description } . OPTIONAL { <%1$s> <%6$s> ?lastEdited }}",
                    synsetIri.stringValue(), SemRes.ID, RDFS.LABEL, CommonIRI.EDGES_DOWNLOADED, RDFS.COMMENT, SemRes.LAST_EDITED);

            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            String id;
            String representation;
            String description = null;
            LocalDateTime lastEdited = null;

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    id = bindingSet.getValue("id").stringValue();
                    representation = bindingSet.getValue("representation").stringValue();
                    edgesLoaded = ((BooleanMemLiteral) bindingSet.getValue("edgesLoaded")).booleanValue();

                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                    }

                    if (bindingSet.getValue("lastEdited") != null) {
                        lastEdited = LocalDateTime.parse(bindingSet.getValue("lastEdited").stringValue(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                    }
                } else {
                    return null;
                }
            }

            // Get removed relations
            queryString = String.format("SELECT ?removedRelationId WHERE { <%s> <%s> ?removedRelation . ?removedRelation <%s> ?removedRelationId }",
                    synsetIri.stringValue(), SemRes.REMOVED_RELATION, SemRes.ID);
            tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            Set<String> removedRelations = new HashSet<>();

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    try {
                        BabelSynsetID removedRelation = new BabelSynsetID(bindingSet.getValue("removedRelationId").stringValue());
                        removedRelations.add(removedRelation.toString());
                    } catch (InvalidBabelSynsetIDException e) {
                        throw new Error("Invalid BabelNet ID");
                    }
                }
            }

            synset = new BabelNetSynset(representation, description, removedRelations, edgesLoaded);
            synset.setId(id);
            if (lastEdited != null) {
                synset.setLastEditedTime(lastEdited);
            }
        }
        return synset;
    }

    @Override
    public String getSynsetClass() {
        return "com.github.semres.babelnet.BabelNetSynset";
    }

    @Override
    public IRI getSynsetClassIri() {
        return CommonIRI.BABELNET_SYNSET;
    }
}
