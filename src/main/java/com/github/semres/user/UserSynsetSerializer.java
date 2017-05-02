package com.github.semres.user;

import com.github.semres.SR;
import com.github.semres.Synset;
import com.github.semres.SynsetSerializer;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class UserSynsetSerializer extends SynsetSerializer {

    public UserSynsetSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }


    @Override
    public Model synsetToRdf(Synset synset) {
        if (!(synset instanceof UserSynset)) {
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
    public UserSynset rdfToSynset(IRI synsetIri) {
        UserSynset synset = null;
        String id;
        String representation;
        String description ;

        String queryString = String.format("SELECT ?id ?representation ?description " +
                        "WHERE { <%1$s> <%2$s> ?id . <%1$s> <%3$s> ?representation . OPTIONAL { <%1$s> <%4$s> ?description }}",
                        synsetIri.stringValue(), SR.ID, RDFS.LABEL, RDFS.COMMENT);

        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    id = bindingSet.getValue("id").stringValue();
                    representation = bindingSet.getValue("representation").stringValue();

                    synset = new UserSynset(representation);
                    synset.setId(id);

                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                        synset = synset.changeDescription(description);
                    }
                }
            }
        }
        return synset;
    }

    @Override
    public UserSynset rdfToSynset(String synsetId) {
        ValueFactory factory = repository.getValueFactory();
        return rdfToSynset(factory.createIRI(baseIri + "synsets/" + synsetId));
    }

    @Override
    public String getSynsetClass() {
        return "com.github.semres.user.UserSynset";
    }

    @Override
    public IRI getSynsetClassIri() {
        return CommonIRI.USER_SYNSET;
    }
}
