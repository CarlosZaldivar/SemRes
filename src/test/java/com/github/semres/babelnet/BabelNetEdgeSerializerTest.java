package com.github.semres.babelnet;

import com.github.semres.*;
import com.github.semres.user.UserSynset;
import com.github.semres.user.UserSynsetSerializer;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;

import static com.github.semres.Utils.createTestRepository;
import static org.junit.Assert.assertTrue;

public class BabelNetEdgeSerializerTest {
    @Test
    public void edgeToRdf() throws Exception {
        String baseIri = "http://example.org/";
        Repository repo = createTestRepository(baseIri);
        repo.initialize();
        ValueFactory factory = repo.getValueFactory();
        EdgeSerializer edgeSerializer = new BabelNetEdgeSerializer(repo, baseIri);

        Synset pointedSynset = new UserSynset("Foo1", "123");
        Synset originSynset = new UserSynset("Foo2", "124");

        RelationType relationType = new BabelNetManager().getRelationTypes().get(0);
        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), relationType, 0.4);
        Model model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, SemRes.ID, factory.createLiteral("124-123")).size() == 1);
        assertTrue(model.filter(null, SemRes.RELATION_TYPE_PROPERTY, factory.createIRI(baseIri + "relationTypes/HOLONYM")).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, null).size() == 0);

        // Check edge with a description
        edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), "Description", relationType, 0.4);
        model = edgeSerializer.edgeToRdf(edge);

        assertTrue(model.filter(null, RDFS.COMMENT, factory.createLiteral("Description")).size() == 1);
    }

    @Test
    public void rdfToEdge() throws Exception {
        String baseIri = "http://example.org/";
        Repository repo = createTestRepository(baseIri);
        BabelNetEdgeSerializer edgeSerializer = new BabelNetEdgeSerializer(repo, baseIri);
        UserSynsetSerializer synsetSerializer = new UserSynsetSerializer(repo, baseIri);

        UserSynset pointedSynset = new UserSynset("Foo1", "123");
        UserSynset originSynset = new UserSynset("Foo2", "124");

        RelationType holonym = new BabelNetManager().getRelationTypes().stream().filter(r -> r.getType().equals("HOLONYM")).findFirst().get();
        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), holonym, 0.5);

        Model model = edgeSerializer.edgeToRdf(edge);
        model.addAll(synsetSerializer.synsetToRdf(originSynset));
        model.addAll(synsetSerializer.synsetToRdf(pointedSynset));

        try (RepositoryConnection connection = repo.getConnection()) {
            connection.add(model);
        }

        edge = edgeSerializer.rdfToEdge(edge.getId());

        assertTrue(edge.getId().equals("124-123"));
        assertTrue(edge.getOriginSynset().equals("124"));
        assertTrue(edge.getPointedSynset().equals("123"));
        assertTrue(edge.getDescription() == null);
        assertTrue(edge.getRelationType().equals(holonym));
        assertTrue(edge.getWeight() == 0.5);
    }
}