package com.github.semres.user;

import com.github.semres.SR;
import com.github.semres.SynsetSerializer;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserSynsetSerializerTest {

    @Test
    public void synsetToRdf() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        ValueFactory factory = repo.getValueFactory();
        SynsetSerializer synsetSerializer = new UserSynsetSerializer(repo, "http://example.org/");

        // Testing basic synset
        UserSynset synset = new UserSynset("123");
        Model model = synsetSerializer.synsetToRdf(synset);

        assertTrue(model.filter(null, SR.ID, factory.createLiteral("123")).size() == 1);
        assertTrue(model.filter(null, RDF.TYPE, factory.createIRI("http://example.org/UserSynset")).size() == 1);
        assertTrue(model.filter(null, RDFS.LABEL, null).size() == 0);
        assertTrue(model.filter(null, RDFS.COMMENT, null).size() == 0);

        synset.setRepresentation("Car");
        synset.setDescription("Type of vehicle.");

        model = synsetSerializer.synsetToRdf(synset);

        assertTrue(model.filter(null, RDFS.LABEL, factory.createLiteral("Car")).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, factory.createLiteral("Type of vehicle.")).size() == 1);
    }

    @Test
    public void rdfToSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        UserSynsetSerializer synsetSerializer = new UserSynsetSerializer(repo, "http://example.org/");

        UserSynset synset = new UserSynset("123");
        synset.setRepresentation("Car");
        synset.setDescription("Type of vehicle.");

        Model model = synsetSerializer.synsetToRdf(synset);

        try (RepositoryConnection connection = repo.getConnection()) {
            connection.add(model);
        }

        synset = synsetSerializer.rdfToSynset("123");
        assertTrue(synset.getId().equals("123"));
        assertTrue(synset.getRepresentation().equals("Car"));
        assertTrue(synset.getDescription().equals("Type of vehicle."));
    }
}