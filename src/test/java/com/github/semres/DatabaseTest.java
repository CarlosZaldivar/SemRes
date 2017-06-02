package com.github.semres;

import com.github.semres.babelnet.*;
import com.github.semres.user.*;
import com.github.semres.user.CommonIRI;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.jlt.util.Language;
import org.eclipse.rdf4j.model.Model;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.github.semres.Utils.createTestDatabase;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DatabaseTest {

    @Test
    public void constructor() throws Exception {
        Database database = createTestDatabase();
        Model model = database.getAllStatements();

        // Dummy test if metadata had been added. It would be better to test if the metadata is correct...
        assertTrue(model.size() > 0);
    }

    @Test
    public void getSynsets() throws Exception {
        Database database = createTestDatabase();

        UserSynset synset1 = new UserSynset("Foo1");
        synset1.setId("123");
        synset1 = synset1.changeDescription("Bar");
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
    public void getSynsetsByType() throws Exception {
        Database database = createTestDatabase();

        UserSynset userSynset = new UserSynset("Foo");
        userSynset.setId("123");
        BabelNetSynset babelNetSynset = new BabelNetSynset("Bar");
        babelNetSynset.setId("124");

        database.addSynset(userSynset);
        database.addSynset(babelNetSynset);

        List<Synset> synsets = database.getSynsets(CommonIRI.USER_SYNSET);
        assertTrue(synsets.size() == 1);
        assertTrue(synsets.get(0).getId().equals("123"));
        synsets = database.getSynsets(com.github.semres.babelnet.CommonIRI.BABELNET_SYNSET);
        assertTrue(synsets.size() == 1);
        assertTrue(synsets.get(0).getId().equals("124"));
    }

    @Test
    public void removeSynset() throws Exception {
        Database database = createTestDatabase();

        UserSynset synset = new UserSynset("Foo");
        synset.setId("123");

        database.addSynset(synset);
        assertTrue(database.getSynsets().size() == 1);
        database.removeSynset(synset);
        assertTrue(database.getSynsets().size() == 0);
    }

    @Test
    public void removeSynsetWithEdges() throws Exception {
        Database database = createTestDatabase();

        UserSynset firstSynset = new UserSynset("Foo");
        firstSynset.setId("123");
        UserSynset middleSynset = new UserSynset("Bar");
        middleSynset.setId("124");
        UserSynset lastSynset = new UserSynset("Car");
        lastSynset.setId("125");

        RelationType relationType = new BabelNetManager().getRelationTypes().get(0);
        Edge firstEdge = new UserEdge(middleSynset.getId(), firstSynset.getId(), relationType, 1);
        Edge secondEdge = new UserEdge(lastSynset.getId(), middleSynset.getId(), relationType, 1);

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
        Database database = createTestDatabase();

        UserSynset originSynset = new UserSynset("Foo");
        originSynset.setId("123");
        UserSynset pointedSynset = new UserSynset("Bar");
        pointedSynset.setId("124");
        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 1);

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(edge);
        assertTrue(database.getOutgoingEdges(originSynset).size() == 1);

        database.removeEdge(edge);
        assertTrue(database.getOutgoingEdges(originSynset).size() == 0);
    }

    @Test
    public void editSynset() throws Exception {
        Database database = createTestDatabase();

        UserSynset originalSynset = new UserSynset("Foo");
        originalSynset.setId("123");
        originalSynset = originalSynset.changeDescription("aaa");

        database.addSynset(originalSynset);
        UserSynset editedSynset = (UserSynset) database.searchSynsets("Foo").get(0);
        editedSynset = editedSynset.changeRepresentation("Bar");
        editedSynset = editedSynset.changeDescription("bbb");

        database.editSynset(editedSynset, originalSynset);

        Synset savedSynset = database.searchSynsets("Bar").get(0);
        assertTrue(savedSynset.getDescription().equals("bbb"));
        assertTrue(database.searchSynsets("Foo").size() == 0);
    }

    @Test
    public void removeBabelNetEdge() throws Exception {
        Database database = createTestDatabase();

        BabelNetSynset originSynset = new BabelNetSynset("Foo");
        originSynset.setId("bn:00024922n");
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar");
        pointedSynset.setId("bn:00024923n");
        Edge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 1);

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(edge);

        originSynset = (BabelNetSynset) database.searchSynsets("Foo").get(0);
        originSynset.setOutgoingEdges(database.getOutgoingEdges(originSynset));

        assertTrue(database.getOutgoingEdges(originSynset).size() == 1);
        assertTrue(originSynset.getRemovedRelations().size() == 0);

        BabelNetSynset editedSynset = originSynset.removeOutgoingEdge(edge.getId());
        database.removeEdge(edge);
        database.editSynset(editedSynset, originSynset);

        assertTrue(database.getOutgoingEdges(editedSynset).size() == 0);
        assertTrue(((BabelNetSynset) database.searchSynsets("Foo").get(0)).getRemovedRelations().size() == 1);
    }

    @Test
    public void searchSynsets() throws Exception {
        Database database = createTestDatabase();

        UserSynset synset1 = new UserSynset("Foo");
        synset1.setId("123");
        synset1 = synset1.changeDescription("Foo1");
        UserSynset synset2 = new UserSynset("Foo");
        synset2.setId("124");
        synset2 = synset2.changeDescription("Foo2");
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
        Database database = createTestDatabase();

        UserSynset originSynset = new UserSynset("Foo1");
        originSynset.setId("123");
        Synset pointedSynset = new UserSynset("Foo2");
        pointedSynset.setId("124");

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(new UserEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 1));

        Synset loadedSynset = database.searchSynsets("Foo1").get(0);

        Edge edge = new ArrayList<>(database.getOutgoingEdges(loadedSynset)).get(0);

        assertTrue(edge.getId().equals("123-124"));
        assertTrue(edge.getPointedSynset().equals(pointedSynset.getId()));
        assertTrue(edge.getDescription() == null);
    }

    @Test
    public void saveBabelNetSynsetWithDownloadedEdges() throws Exception {
        // BabelSynset objects are used internally to download edges from BabelNet. We use mocking framework
        // to avoid making API calls during tests.
        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);

        BabelSense mockBabelSense = Mockito.mock(BabelSense.class);
        when(mockBabelSense.getSenseString()).thenReturn("Foo");

        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID("bn:00024922n"));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        BabelNetSynset synset = new BabelNetSynset(mockBabelSynset);
        synset = synset.loadEdgesFromBabelNet();
        assertTrue(synset.isDownloadedWithEdges());

        Database database = createTestDatabase();
        database.addSynset(synset);
        assertTrue(((BabelNetSynset) database.searchSynsets("Foo").get(0)).isDownloadedWithEdges());
    }

    @Test
    public void hasSynset() throws Exception {
        Database database = createTestDatabase();

        assertFalse(database.hasSynset("123"));

        UserSynset synset = new UserSynset("Foo");
        synset.setId("123");
        database.addSynset(synset);

        assertTrue(database.hasSynset("123"));
    }

    @Test
    public void hasEdge() throws Exception {
        Database database = createTestDatabase();
        assertFalse(database.hasEdge("123-124"));

        UserSynset originSynset = new UserSynset("Foo");
        originSynset.setId("123");
        UserSynset pointedSynset = new UserSynset("Bar");
        pointedSynset.setId("124");
        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 1);
        database.addEdge(edge);

        assertTrue(database.hasEdge(edge.getId()));
    }

    @Test
    public void generateNewSynsetId() throws Exception {
        Database database = createTestDatabase();
        String id = database.generateNewSynsetId();
        assertTrue(id.length() == 26);
    }

    @Test
    public void addRelationType() throws Exception {
        Database database = createTestDatabase();
        int relationsNumber = database.getRelationTypes().size();
        RelationType relationType = new RelationType("RelationX", "User");
        database.addRelationType(relationType);
        Collection<RelationType> relationTypes = database.getRelationTypes();
        assertTrue(relationTypes.size() == relationsNumber + 1);
        assertTrue(relationTypes.stream().map(RelationType::getSource).anyMatch("User"::equals));
        assertTrue(relationTypes.stream().map(RelationType::getType).anyMatch("RelationX"::equals));
    }

    @Test(expected = RuntimeException.class)
    public void addRelationTypesWithSameName() throws Exception {
        Database database = createTestDatabase();
        RelationType relationType = new RelationType("RelationX", "User");
        database.addRelationType(relationType);
        database.addRelationType(relationType);
    }

    @Test
    public void removeRelationType() throws Exception {
        Database database = createTestDatabase();
        int relationsNumber = database.getRelationTypes().size();
        RelationType relationType = new RelationType("RelationX", "User");
        database.addRelationType(relationType);
        database.removeRelationType(relationType);
        Collection<RelationType> relationTypes = database.getRelationTypes();
        assertTrue(relationTypes.size() == relationsNumber);
    }

    @Test(expected = RuntimeException.class)
    public void removeUsedRelationType() throws Exception {
        Database database = createTestDatabase();
        RelationType relationType = new RelationType("RelationX", "User");
        database.addRelationType(relationType);

        UserSynset originSynset = new UserSynset("Foo");
        originSynset.setId("123");
        UserSynset pointedSynset = new UserSynset("Bar");
        pointedSynset.setId("124");
        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), relationType, 1);

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(edge);

        database.removeRelationType(relationType);
    }
}