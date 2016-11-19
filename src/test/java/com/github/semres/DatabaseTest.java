package com.github.semres;

import com.github.semres.user.UserEdge;
import com.github.semres.user.UserEdgeSerializer;
import com.github.semres.user.UserSynset;
import com.github.semres.user.UserSynsetSerializer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseTest {

    @Test
    public void constructor() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, new ArrayList<>(), repo);
    }

    @Test
    public void getSynsets() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, new ArrayList<>(), repo);

        UserSynset synset1 = new UserSynset("Foo1");
        synset1.setId("123");
        synset1.setDescription("Bar");
        Synset synset2 = new UserSynset("Foo2");
        synset2.setId("124");

        database.addSynset(synset1);
        database.addSynset(synset2);

        List<Synset> synsets = database.getSynsets();

        assertTrue(synsets.size() == 2);

        for (Synset synset : synsets) {
            if (synset.getId().equals("123")) {
                assertTrue(synset.getRepresentation().equals("Foo1"));
                assertTrue(synset.getDescription().equals("Bar"));
            } else if (synset.getId().equals("124")) {
                assertTrue(synset.getRepresentation().equals("Foo2"));
                assertTrue(synset.getDescription() == null);
            } else {
                throw new Exception();
            }
        }
    }

    @Test
    public void removeSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> synsetSerializerClasses = new ArrayList<>();
        synsetSerializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(synsetSerializerClasses, new ArrayList<>(), repo);

        UserSynset synset = new UserSynset("Foo");
        synset.setId("123");

        database.addSynset(synset);
        assertTrue(database.getSynsets().size() == 1);
        database.removeSynset(synset);
        assertTrue(database.getSynsets().size() == 0);
    }

    @Test
    public void searchSynsets() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, new ArrayList<>(), repo);

        UserSynset synset1 = new UserSynset("Foo");
        synset1.setId("123");
        synset1.setDescription("Foo1");
        UserSynset synset2 = new UserSynset("Foo");
        synset2.setId("124");
        synset2.setDescription("Foo2");
        UserSynset synset3 = new UserSynset("Bar");
        synset3.setId("125");

        database.addSynset(synset1);
        database.addSynset(synset2);
        database.addSynset(synset3);

        List<Synset> synsets = database.searchSynsets("Foo");

        assertTrue(synsets.size() == 2);

        for (Synset synset : synsets) {
            if (synset.getId().equals("123")) {
                assertTrue(synset.getRepresentation().equals("Foo"));
                assertTrue(synset.getDescription().equals("Foo1"));
            } else if (synset.getId().equals("124")) {
                assertTrue(synset.getRepresentation().equals("Foo"));
                assertTrue(synset.getDescription().equals("Foo2"));
            } else {
                throw new Exception();
            }
        }
    }

    @Test
    public void loadEdges() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> synsetSerializers = new ArrayList<>();
        synsetSerializers.add(UserSynsetSerializer.class);
        List<Class> edgeSerializers = new ArrayList<>();
        edgeSerializers.add(UserEdgeSerializer.class);

        Database database = new Database(synsetSerializers, edgeSerializers, repo);

        UserSynset originSynset = new UserSynset("Foo1");
        originSynset.setId("123");
        Synset pointedSynset = new UserSynset("Foo2");
        pointedSynset.setId("124");

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(new UserEdge(pointedSynset, originSynset, Edge.RelationType.OTHER, 1));

        Synset loadedSynset = database.searchSynsets("Foo1").get(0);

        database.loadEdges(loadedSynset);

        Edge edge = new ArrayList<>(loadedSynset.getEdges().values()).get(0);

        assertTrue(edge.getId().equals("123-124"));
        assertTrue(edge.getPointedSynset().getId().equals(pointedSynset.getId()));
        assertTrue(edge.getDescription() == null);
    }
}