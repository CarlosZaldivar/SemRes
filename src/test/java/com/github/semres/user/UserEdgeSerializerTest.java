package com.github.semres.user;

import com.github.semres.*;
import com.github.semres.babelnet.BabelNetManager;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;

import static com.github.semres.Utils.createTestRepository;
import static org.junit.Assert.*;

public class UserEdgeSerializerTest {
    @Test
    public void edgeToRdf() throws Exception {
        String baseIri = "http://example.org/";
        Repository repo = createTestRepository(baseIri);
        ValueFactory factory = repo.getValueFactory();
        EdgeSerializer edgeSerializer = new UserEdgeSerializer(repo, baseIri);

        Synset pointedSynset = new UserSynset("Foo1", "123");
        Synset originSynset = new UserSynset("Foo2", "124");

        UserEdge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 0.4);
        Model model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, SemRes.ID, factory.createLiteral("124-123")).size() == 1);
        assertTrue(model.filter(null, SemRes.RELATION_TYPE_PROPERTY, factory.createIRI(baseIri + "relationTypes/HOLONYM")).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, null).size() == 0);

        edge = edge.changeDescription("Description");

        model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, RDFS.COMMENT, factory.createLiteral("Description")).size() == 1);
    }

    @Test
    public void rdfToEdge() throws Exception {
        String baseIri = "http://example.org/";
        Repository repo = createTestRepository(baseIri);
        UserEdgeSerializer edgeSerializer = new UserEdgeSerializer(repo, baseIri);
        UserSynsetSerializer synsetSerializer = new UserSynsetSerializer(repo, baseIri);

        UserSynset pointedSynset = new UserSynset("Foo1", "123");
        UserSynset originSynset = new UserSynset("Foo2", "124");

        UserEdge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 0.5);

        Model model = edgeSerializer.edgeToRdf(edge);
        model.addAll(synsetSerializer.synsetToRdf(originSynset));
        model.addAll(synsetSerializer.synsetToRdf(pointedSynset));

        try (RepositoryConnection connection = repo.getConnection()) {
            connection.add(model);
        }

        edge = edgeSerializer.rdfToEdge("124-123");

        assertTrue(edge.getId().equals("124-123"));
        assertTrue(edge.getOriginSynsetId().equals("124"));
        assertTrue(edge.getPointedSynsetId().equals("123"));
        assertTrue(edge.getDescription() == null);
        assertTrue(edge.getRelationType().getType().equals("HOLONYM"));
        assertTrue(edge.getWeight() == 0.5);
    }
}