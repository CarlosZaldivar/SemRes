package com.github.semres.user;

import com.github.semres.SemRes;
import com.github.semres.Synset;
import com.github.semres.SynsetSerializer;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class UserSynsetSerializer extends SynsetSerializer {

    public UserSynsetSerializer(String baseIri) {
        super(baseIri);
    }

    @Override
    public Model synsetToRdf(Synset synset) {
        if (!(synset instanceof UserSynset)) {
            throw new IllegalArgumentException();
        }

        Model model = new LinkedHashModel();
        ValueFactory factory = SimpleValueFactory.getInstance();

        IRI synsetIri =
                factory.createIRI(baseIri + "synsets/" + synset.getId());

        model.add(synsetIri, RDF.TYPE, getSynsetClassIri());
        Literal id = factory.createLiteral(synset.getId());
        model.add(synsetIri, SemRes.ID, id);
        Literal representation = factory.createLiteral(synset.getRepresentation());
        model.add(factory.createStatement(synsetIri, RDFS.LABEL, representation));

        if (synset.getDescription() != null) {
            Literal description = factory.createLiteral(synset.getDescription());
            model.add(factory.createStatement(synsetIri, RDFS.COMMENT, description));
        }

        if (synset.getLastEditedTime() != null) {
            model.add(synsetIri, SemRes.LAST_EDITED, factory.createLiteral(Date.from(synset.getLastEditedTime().atZone(ZoneId.systemDefault()).toInstant())));
        }
        return model;
    }

    @Override
    public UserSynset rdfToSynset(IRI synsetIri, Repository repository) {
        UserSynset synset = null;
        String id;
        String representation;
        String description = null;

        String queryString = String.format("SELECT ?id ?representation ?description ?lastEdited " +
                        "WHERE { <%1$s> <%2$s> ?id . <%1$s> <%3$s> ?representation . " +
                        "OPTIONAL { <%1$s> <%4$s> ?description } . OPTIONAL { <%1$s> <%5$s> ?lastEdited }}",
                        synsetIri.stringValue(), SemRes.ID, RDFS.LABEL, RDFS.COMMENT, SemRes.LAST_EDITED);

        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    id = bindingSet.getValue("id").stringValue();
                    representation = bindingSet.getValue("representation").stringValue();
                    LocalDateTime lastEdited = null;


                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                    }
                    if (bindingSet.getValue("lastEdited") != null) {
                        lastEdited = LocalDateTime.parse(bindingSet.getValue("lastEdited").stringValue(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                    }

                    synset = new UserSynset(representation, id, description, lastEdited);
                }
            }
        }
        return synset;
    }

    @Override
    public UserSynset rdfToSynset(String synsetId, Repository repository) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        return rdfToSynset(factory.createIRI(baseIri + "synsets/" + synsetId), repository);
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
