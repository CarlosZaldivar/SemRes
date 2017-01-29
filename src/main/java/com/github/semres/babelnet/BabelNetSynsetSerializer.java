package com.github.semres.babelnet;

import com.github.semres.SR;
import com.github.semres.SynsetSerializer;
import com.github.semres.Synset;
import com.github.semres.user.UserSynset;
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

        model.add(getSynsetClassIri(), RDF.TYPE, RDFS.CLASS);
        model.add(getSynsetClassIri(), RDFS.SUBCLASSOF, SR.SYNSET);

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

        return model;
    }

    @Override
    public Synset rdfToSynset(String synsetId) {
        ValueFactory factory = repository.getValueFactory();
        return rdfToSynset(factory.createIRI(baseIri + "synsets/" + synsetId));
    }

    @Override
    public Synset rdfToSynset(IRI synsetIri) {
        BabelNetSynset synset = null;
        String id;
        String representation;
        String description ;

        String queryString = String.format("SELECT ?id ?representation ?description " +
                        "WHERE { <%s> <%s> ?id . <%s> <%s> ?representation . OPTIONAL { <%s> <%s> ?description }}",
                synsetIri.stringValue(), SR.ID, synsetIri.stringValue(), RDFS.LABEL, synsetIri.stringValue(), RDFS.COMMENT);

        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    id = bindingSet.getValue("id").stringValue();
                    representation = bindingSet.getValue("representation").stringValue();

                    synset = new BabelNetSynset(representation);
                    synset.setId(id);

                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                        synset.setDescription(description);
                    }
                }
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
        return repository.getValueFactory().createIRI(baseIri + "classes/BabelNetSynset");
    }
}
