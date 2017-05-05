package com.github.semres.babelnet;

import com.github.semres.SR;
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

        model.add(synsetIri, RDF.TYPE, getSynsetClassIri());
        model.add(synsetIri, SR.ID, factory.createLiteral(synset.getId()));

        for (String removedRelation : ((BabelNetSynset) synset).getRemovedRelations()) {
            IRI removedRelationIRI = factory.createIRI(baseIri + "synsets/" + synset.getId() + "/removedRelations/" + removedRelation);
            model.add(factory.createStatement(removedRelationIRI, SR.ID, factory.createLiteral(removedRelation)));
            model.add(factory.createStatement(synsetIri, SR.REMOVED_RELATION, removedRelationIRI));
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
        String id;
        String representation;
        String description = null;
        boolean edgesLoaded;

        try (RepositoryConnection conn = repository.getConnection()) {

            // Get synset representation, id, description and the flag if the edges from BabelNet were loaded or not
            String queryString = String.format("SELECT ?id ?representation ?edgesLoaded ?description " +
                            "WHERE { <%1$s> <%2$s> ?id . <%1$s> <%3$s> ?representation . <%1$s> <%4$s> ?edgesLoaded . OPTIONAL { <%1$s> <%5$s> ?description }}",
                    synsetIri.stringValue(), SR.ID, RDFS.LABEL, CommonIRI.EDGES_DOWNLOADED, RDFS.COMMENT);


            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    id = bindingSet.getValue("id").stringValue();
                    representation = bindingSet.getValue("representation").stringValue();
                    edgesLoaded = ((BooleanMemLiteral) bindingSet.getValue("edgesLoaded")).booleanValue();

                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                    }
                } else {
                    return null;
                }
            }

            // Get removed relations
            queryString = String.format("SELECT ?removedRelationId WHERE { <%s> <%s> ?removedRelation . ?removedRelation <%s> ?removedRelationId }",
                    synsetIri.stringValue(), SR.REMOVED_RELATION, SR.ID);
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

            synset = new BabelNetSynset(representation, removedRelations, edgesLoaded);
            synset.setId(id);
            if (description != null) {
                synset.setDescription(description);
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
