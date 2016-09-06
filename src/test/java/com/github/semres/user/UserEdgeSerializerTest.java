package com.github.semres.user;

import com.github.semres.*;
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

public class UserEdgeSerializerTest {
    @Test
    public void edgeToRdf() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        ValueFactory factory = repo.getValueFactory();
        EdgeSerializer edgeSerializer = new UserEdgeSerializer(repo, "http://example.org/");

        Synset pointedSynset = new UserSynset("123");
        Synset originSynset = new UserSynset("124");

        UserEdge edge = new UserEdge(pointedSynset, originSynset, Edge.RelationType.HOLONYM, 0.4);
        Model model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, SR.ID, factory.createLiteral("124-123")).size() == 1);
        assertTrue(model.filter(null, SR.RELATION_TYPE, SR.HOLONYM).size() == 1);
        assertTrue(model.filter(null, RDF.TYPE, factory.createIRI("http://example.org/classes/UserEdge")).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, null).size() == 0);

        edge.setDescription("Description");

        model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, RDFS.COMMENT, factory.createLiteral("Description")).size() == 1);
    }

    @Test
    public void rdfToEdge() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        UserEdgeSerializer edgeSerializer = new UserEdgeSerializer(repo, "http://example.org/");

        UserSynset pointedSynset = new UserSynset("123");
        UserSynset originSynset = new UserSynset("124");

        UserEdge edge = new UserEdge(pointedSynset, originSynset, Edge.RelationType.HOLONYM, 0.5);

        Model model = edgeSerializer.edgeToRdf(edge);

        try (RepositoryConnection connection = repo.getConnection()) {
            connection.add(model);
        }

        edge = edgeSerializer.rdfToEdge("124-123", pointedSynset, originSynset);

        assertTrue(edge.getId().equals("124-123"));
        assertTrue(edge.getOriginSynset().getId().equals("124"));
        assertTrue(edge.getPointedSynset().getId().equals("123"));
        assertTrue(edge.getDescription() == null);
        assertTrue(edge.getRelationType() == Edge.RelationType.HOLONYM);
        assertTrue(edge.getWeight() == 0.5);
    }
}