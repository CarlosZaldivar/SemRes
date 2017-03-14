package com.github.semres;

import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetEdgeSerializer;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.babelnet.BabelNetSynsetSerializer;
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
        List<Class<? extends SynsetSerializer>> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        new Database(serializerClasses, new ArrayList<>(), repo);
    }

    @Test
    public void getSynsets() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> serializerClasses = new ArrayList<>();
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
            switch (synset.getId()) {
                case "123":
                    assertTrue(synset.getRepresentation().equals("Foo1"));
                    assertTrue(synset.getDescription().equals("Bar"));
                    break;
                case "124":
                    assertTrue(synset.getRepresentation().equals("Foo2"));
                    assertTrue(synset.getDescription() == null);
                    break;
                default:
                    throw new Exception();
            }
        }
    }

    @Test
    public void removeSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
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
    public void removeSynsetWithEdges() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
        synsetSerializerClasses.add(UserSynsetSerializer.class);
        List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();
        edgeSerializerClasses.add(UserEdgeSerializer.class);

        Database database = new Database(synsetSerializerClasses, edgeSerializerClasses, repo);

        UserSynset firstSynset = new UserSynset("Foo");
        firstSynset.setId("123");
        UserSynset middleSynset = new UserSynset("Bar");
        middleSynset.setId("124");
        UserSynset lastSynset = new UserSynset("Car");
        lastSynset.setId("125");

        Edge firstEdge = new UserEdge(middleSynset, firstSynset, Edge.RelationType.HOLONYM, 1);
        Edge secondEdge = new UserEdge(lastSynset, middleSynset, Edge.RelationType.HOLONYM, 1);

        database.addSynset(firstSynset);
        database.addSynset(middleSynset);
        database.addSynset(lastSynset);
        database.addEdge(firstEdge);
        database.addEdge(secondEdge);
        assertTrue(database.getOutgoingEdges(firstSynset).size() == 1 && database.getPointingEdges(lastSynset).size() == 1);

        database.removeSynset(middleSynset);

        assertTrue(database.getOutgoingEdges(firstSynset).size() == 0 && database.getPointingEdges(lastSynset).size() == 0);
    }

    @Test
    public void removeEdge() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
        synsetSerializerClasses.add(UserSynsetSerializer.class);
        List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();
        edgeSerializerClasses.add(UserEdgeSerializer.class);

        Database database = new Database(synsetSerializerClasses, edgeSerializerClasses, repo);

        UserSynset originSynset = new UserSynset("Foo");
        originSynset.setId("123");
        UserSynset pointedSynset = new UserSynset("Bar");
        pointedSynset.setId("124");
        Edge edge = new UserEdge(pointedSynset, originSynset, Edge.RelationType.HOLONYM, 1);

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(edge);
        assertTrue(database.getOutgoingEdges(originSynset).size() == 1);

        database.removeEdge(edge);
        assertTrue(database.getOutgoingEdges(originSynset).size() == 0);
    }

    @Test
    public void editSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
        synsetSerializerClasses.add(UserSynsetSerializer.class);
        List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();
        edgeSerializerClasses.add(UserEdgeSerializer.class);

        Database database = new Database(synsetSerializerClasses, edgeSerializerClasses, repo);

        UserSynset originalSynset = new UserSynset("Foo");
        originalSynset.setId("123");
        originalSynset.setDescription("aaa");

        database.addSynset(originalSynset);
        UserSynset editedSynset = (UserSynset) database.searchSynsets("Foo").get(0);
        editedSynset.setRepresentation("Bar");
        editedSynset.setDescription("bbb");

        database.editSynset(editedSynset, originalSynset);

        Synset savedSynset = database.searchSynsets("Bar").get(0);
        assertTrue(savedSynset.getDescription().equals("bbb"));
        assertTrue(database.searchSynsets("Foo").size() == 0);
    }

    @Test
    public void removeBabelNetEdge() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
        synsetSerializerClasses.add(BabelNetSynsetSerializer.class);
        List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();
        edgeSerializerClasses.add(BabelNetEdgeSerializer.class);

        Database database = new Database(synsetSerializerClasses, edgeSerializerClasses, repo);

        BabelNetSynset originSynset = new BabelNetSynset("Foo");
        originSynset.setId("bn:00024922n");
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar");
        pointedSynset.setId("bn:00024923n");
        Edge edge = new BabelNetEdge(pointedSynset, originSynset, Edge.RelationType.HOLONYM, "", 1);

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(edge);

        assertTrue(database.getOutgoingEdges(originSynset).size() == 1);
        assertTrue(originSynset.getRemovedRelations().size() == 0);

        BabelNetSynset editedSynset = (BabelNetSynset) database.searchSynsets("Foo").get(0);
        editedSynset.setOutgoingEdges(database.getOutgoingEdges(editedSynset));
        editedSynset.removeOutgoingEdge(edge.getId());
        database.removeEdge(edge);
        database.editSynset(editedSynset, originSynset);

        assertTrue(database.getOutgoingEdges(editedSynset).size() == 0);
        assertTrue(((BabelNetSynset) database.searchSynsets("Foo").get(0)).getRemovedRelations().size() == 1);
    }

    @Test
    public void searchSynsets() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> serializerClasses = new ArrayList<>();
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
            switch (synset.getId()) {
                case "123":
                    assertTrue(synset.getRepresentation().equals("Foo"));
                    assertTrue(synset.getDescription().equals("Foo1"));
                    break;
                case "124":
                    assertTrue(synset.getRepresentation().equals("Foo"));
                    assertTrue(synset.getDescription().equals("Foo2"));
                    break;
                default:
                    throw new Exception();
            }
        }
    }

    @Test
    public void getOutgoingEdges() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializers = new ArrayList<>();
        synsetSerializers.add(UserSynsetSerializer.class);
        List<Class<? extends EdgeSerializer>> edgeSerializers = new ArrayList<>();
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

        Edge edge = new ArrayList<>(database.getOutgoingEdges(loadedSynset)).get(0);

        assertTrue(edge.getId().equals("123-124"));
        assertTrue(edge.getPointedSynset().getId().equals(pointedSynset.getId()));
        assertTrue(edge.getDescription() == null);
    }
}