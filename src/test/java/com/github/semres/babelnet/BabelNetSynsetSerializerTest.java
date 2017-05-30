package com.github.semres.babelnet;

import com.github.semres.Edge;
import com.github.semres.SemRes;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BabelNetSynsetSerializerTest {
    private String baseIri = "http://example.org/";

    @Test
    public void synsetToRdf() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        ValueFactory factory = repo.getValueFactory();
        BabelNetSynsetSerializer synsetSerializer = new BabelNetSynsetSerializer(repo, baseIri);

        // Testing basic synset
        BabelNetSynset synset = new BabelNetSynset("Car");
        synset.setId("123");
        Model model = synsetSerializer.synsetToRdf(synset);

        assertTrue(model.filter(null, SemRes.ID, factory.createLiteral("123")).size() == 1);
        assertTrue(model.filter(null, RDFS.LABEL, null).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, null).size() == 0);

        synset.setDescription("Type of vehicle.");

        model = synsetSerializer.synsetToRdf(synset);

        assertTrue(model.filter(null, RDFS.LABEL, factory.createLiteral("Car")).size() == 1);
        assertTrue(model.filter(null, RDFS.COMMENT, factory.createLiteral("Type of vehicle.")).size() == 1);

        // Testing BabelNetEdge removal.
        BabelNetSynset synsetWithEdge = synset.addOutgoingEdge(new BabelNetEdge("124", "123",
                                                               new BabelNetManager().getRelationTypes().get(0), 1));
        BabelNetSynset synsetWithRemovedEdge = synsetWithEdge.removeOutgoingEdge("123-124");

        model = synsetSerializer.synsetToRdf(synsetWithRemovedEdge);

        assertTrue(model.filter(null, SemRes.REMOVED_RELATION,
                factory.createIRI(baseIri + "synsets/" + synset.getId() + "/removedRelations/" + "124")).size() == 1);
    }

    @Test
    public void rdfToSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        BabelNetSynsetSerializer synsetSerializer = new BabelNetSynsetSerializer(repo, baseIri);

        BabelNetSynset synset = new BabelNetSynset("Car");
        synset.setId("123");
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