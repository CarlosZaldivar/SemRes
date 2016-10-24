package com.github.semres.user;

import com.github.semres.SR;
import com.github.semres.Synset;
import com.github.semres.SynsetSerializer;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
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

        model.add(getUserSynsetClassIri(), RDF.TYPE, RDFS.CLASS);
        model.add(getUserSynsetClassIri(), RDFS.SUBCLASSOF, SR.SYNSET);

        IRI synsetIri = factory.createIRI(baseIri + "synsets/" + synset.getId());

        if (synset.getRepresentation() != null) {
            Literal representation = factory.createLiteral(synset.getRepresentation());
            model.add(factory.createStatement(synsetIri, RDFS.LABEL, representation));
        }

        if (synset.getDescription() != null) {
            Literal description = factory.createLiteral(synset.getDescription());
            model.add(factory.createStatement(synsetIri, RDFS.COMMENT, description));
        }

        model.add(synsetIri, RDF.TYPE, getUserSynsetClassIri());
        model.add(synsetIri, SR.ID, factory.createLiteral(synset.getId()));

        return model;
    }

    @Override
    public IRI getUserSynsetClassIri() {
        return repository.getValueFactory().createIRI(baseIri + "classes/UserSynset");
    }

    @Override
    public UserSynset rdfToSynset(String synsetId) {
        ValueFactory factory = repository.getValueFactory();
        String id;
        String representation = null;
        String description = null;
        try (RepositoryConnection connection = repository.getConnection()) {
            Model model = QueryResults.asModel(connection.getStatements(factory.createIRI(baseIri + "synsets/" + synsetId), null, null));
            if (model.size() == 0) {
                throw new IllegalArgumentException();
            }

            id = model.filter(null, SR.ID, null).objects().iterator().next().stringValue();

            for (Value repr: model.filter(null, RDFS.LABEL, null).objects()) {
                representation = repr.stringValue();
                break;
            }

            for (Value desc: model.filter(null, RDFS.COMMENT, null).objects()) {
                description = desc.stringValue();
                break;
            }
        }

        UserSynset synset = new UserSynset(representation);
        synset.setId(id);

        if (description != null) {
            synset.setDescription(description);
        }

        return synset;
    }

    @Override
    public String getSynsetClass() {
        return "com.github.semres.user.UserSynset";
    }
}
