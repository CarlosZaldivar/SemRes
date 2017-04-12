package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.EdgeSerializer;
import com.github.semres.SR;
import com.github.semres.Synset;
import com.github.semres.user.UserSynset;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BabelNetEdgeSerializerTest {
    @Test
    public void edgeToRdf() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        ValueFactory factory = repo.getValueFactory();
        EdgeSerializer edgeSerializer = new BabelNetEdgeSerializer(repo, "http://example.org/");

        Synset pointedSynset = new UserSynset("Foo1");
        Synset originSynset = new UserSynset("Foo2");

        pointedSynset.setId("123");
        originSynset.setId("124");

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), Edge.RelationType.HOLONYM, 0.4);
        Model model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, SR.ID, factory.createLiteral("124-123")).size() == 1);
        assertTrue(model.filter(null, SR.RELATION_TYPE, SR.HOLONYM).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, null).size() == 0);

        model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, RDFS.COMMENT, factory.createLiteral("Description")).size() == 1);
    }

    @Test
    public void rdfToEdge() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        BabelNetEdgeSerializer edgeSerializer = new BabelNetEdgeSerializer(repo, "http://example.org/");

        UserSynset pointedSynset = new UserSynset("Foo1");
        UserSynset originSynset = new UserSynset("Foo2");

        pointedSynset.setId("123");
        originSynset.setId("124");

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), Edge.RelationType.HOLONYM, 0.5);

        Model model = edgeSerializer.edgeToRdf(edge);

        try (RepositoryConnection connection = repo.getConnection()) {
            connection.add(model);
        }

        edge = edgeSerializer.rdfToEdge("124-123");

        assertTrue(edge.getId().equals("124-123"));
        assertTrue(edge.getOriginSynset().equals("124"));
        assertTrue(edge.getPointedSynset().equals("123"));
        assertTrue(edge.getDescription() == null);
        assertTrue(edge.getRelationType() == Edge.RelationType.HOLONYM);
        assertTrue(edge.getWeight() == 0.5);
    }
}