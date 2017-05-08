package com.github.semres;


import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.gui.IDAlreadyTakenException;
import com.github.semres.user.UserEdge;
import com.github.semres.user.UserSynset;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BoardTest {

    @Test
    public void addSynset() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo");
        board.addSynset(synset);
        board.save();

        assertTrue(database.searchSynsets("Foo").size() == 1);
    }

    @Test
    public void removeSynset() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo");
        synset.setId("123");
        board.addSynset(synset);
        board.save();
        board.removeSynset("123");
        board.save();
        assertTrue(database.searchSynsets("Foo").size() == 0);
    }

    @Test
    public void removeEdge() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        Synset originSynset = new UserSynset("Foo");
        originSynset.setId("123");
        Synset pointedSynset = new UserSynset("Bar");
        pointedSynset.setId("124");

        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), Edge.RelationType.HOLONYM, 0);
        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();
        board.removeEdge("123-124");
        board.save();

        Synset loadedSynset = database.searchSynsets("Foo").get(0);
        loadedSynset.setOutgoingEdges(database.getOutgoingEdges(loadedSynset));

        assertTrue(loadedSynset.getOutgoingEdges().size() == 0);
    }

    @Test
    public void addRemovedBabelnetEdge() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        Synset originSynset = new BabelNetSynset("Foo");
        originSynset.setId("bn:00024922n");
        Synset pointedSynset = new BabelNetSynset("Bar");
        pointedSynset.setId("bn:00024923n");

        Edge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), Edge.RelationType.HOLONYM, 0);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();
        board.removeEdge("bn:00024922n-bn:00024923n");
        board.save();
        // Add removed edge again
        board.addEdge(edge);
        board.save();

        Synset loadedSynset = database.searchSynsets("Foo").get(0);
        loadedSynset.setOutgoingEdges(database.getOutgoingEdges(loadedSynset));

        assertTrue(loadedSynset.getOutgoingEdges().size() == 0);
    }

    @Test
    public void addSameSynsetMultipleTimes() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        Synset synset = new BabelNetSynset("Foo");
        synset.setId("bn:00024922n");
        board.addSynset(synset);
        board.save();

        try {
            board.addSynset(synset);
            throw new Exception("IDAlreadyTakenException not thrown.");
        } catch (IDAlreadyTakenException e) {}

        Board newBoard = new Board(database);



        try {
            newBoard.addSynset(synset);
            throw new Exception("IDAlreadyTakenException not thrown.");
        } catch (IDAlreadyTakenException e) {}
    }

    @Test
    public void addSameEdgeMultipleTimes() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);


        Synset originSynset = new BabelNetSynset("Foo");
        originSynset.setId("bn:00024922n");
        Synset pointedSynset = new BabelNetSynset("Bar");
        pointedSynset.setId("bn:00024923n");

        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), Edge.RelationType.HOLONYM, 1);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();

        try {
            board.addEdge(edge);
            throw new Exception("IDAlreadyTakenException not thrown.");
        } catch (IDAlreadyTakenException e) {}

        Board newBoard = new Board(database);
        try {
            newBoard.addEdge(edge);
            throw new Exception("IDAlreadyTakenException not thrown.");
        } catch (IDAlreadyTakenException e) {}
    }

    @Test
    public void editSynset() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        UserSynset synset = new UserSynset("Foo", "Bar");
        board.addSynset(synset);
        board.save();
        assertTrue(database.getSynset(synset.getId()).getDescription().equals("Bar"));
        UserSynset editedSynset = synset.changeDescription("Car");
        board.editSynset(synset, editedSynset);
        assertTrue(board.getSynset(synset.getId()) == editedSynset);
        board.save();

        assertTrue(database.getSynset(synset.getId()).getDescription().equals("Car"));
    }

    @Test
    public void editEdge() throws Exception {
        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database);

        UserSynset originSynset = new UserSynset("Foo");
        UserSynset pointedSynset = new UserSynset("Bar");
        board.addSynset(originSynset);
        board.addSynset(pointedSynset);

        UserEdge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), "Description 1", Edge.RelationType.HOLONYM, 0);
        board.addEdge(edge);
        board.save();

        UserEdge savedEdge = (UserEdge) database.getOutgoingEdges(originSynset).get(0);
        assertTrue(savedEdge.getRelationType() == Edge.RelationType.HOLONYM);
        assertTrue(savedEdge.getDescription().equals("Description 1"));
        assertTrue(savedEdge.getWeight() == 0);

        UserEdge editedEdge = edge.changeRelationType(Edge.RelationType.MERONYM).changeWeight(1).changeDescription("Description 2");
        board.editEdge(edge, editedEdge);
        assertTrue(board.getEdge(editedEdge.getId()) == editedEdge);
        board.save();
        assertTrue(database.getOutgoingEdges(originSynset).size() == 1);

        savedEdge = (UserEdge) database.getOutgoingEdges(originSynset).get(0);
        assertTrue(savedEdge.relationType == Edge.RelationType.MERONYM);
        assertTrue(savedEdge.getDescription().equals("Description 2"));
        assertTrue(savedEdge.getWeight() == 1);
    }


    @Test
    public void update() throws Exception {
        BabelNetSynset originalSynset = new BabelNetSynset("Foo");
        originalSynset.setId("bn:00024922n");
        originalSynset.setDescription("Description 1");


        BabelSense mockBabelSense = Mockito.mock(BabelSense.class);
        when(mockBabelSense.getSenseString()).thenReturn("Bar");

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID("bn:00024922n"));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        BabelNetSynset updatedSynset = new BabelNetSynset(mockBabelSynset);
        updatedSynset.setDescription("Description 2");

        BabelNetManager mockManager = Mockito.mock(BabelNetManager.class);
        when(mockManager.getSynset("bn:00024922n")).thenReturn(updatedSynset);

        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originalSynset);
        board.save();

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset("bn:00024922n").getRepresentation().equals("Bar"));

        BabelNetSynset savedSynset = (BabelNetSynset) database.getSynset("bn:00024922n");
        assertTrue(savedSynset.getRepresentation().equals("Bar"));
        assertTrue(savedSynset.getDescription().equals("Description 2"));
    }

    @Test
    public void updateSynsetByAddingEdge() throws Exception {
        String originSynsetId = "bn:00024922n";
        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo");
        originSynset.setId(originSynsetId);
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar");
        pointedSynset.setId(pointedSynsetId);

        BabelNetSynset updatedOriginSynset = getMockOriginBabelNetSynset(originSynsetId, pointedSynsetId);
        BabelNetSynset updatedPointedSynset = getMockPointedBabelNetSynset(pointedSynsetId);

        BabelNetManager mockManager = Mockito.mock(BabelNetManager.class);
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(updatedPointedSynset);

        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.save();

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);
    }

    @Test
    public void updateSynsetByAddingEdgeWithNewSynset() throws Exception {
        String originSynsetId = "bn:00024922n";
        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo");
        originSynset.setId(originSynsetId);
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar");
        pointedSynset.setId(pointedSynsetId);

        BabelNetSynset updatedOriginSynset = getMockOriginBabelNetSynset(originSynsetId, pointedSynsetId);
        BabelNetSynset updatedPointedSynset = getMockPointedBabelNetSynset(pointedSynsetId);

        BabelNetManager mockManager = Mockito.mock(BabelNetManager.class);
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(updatedPointedSynset);

        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.save();
        assertTrue(database.getSynsets().size() == 1);

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);
        board.save();
        assertTrue(database.getSynsets().size() == 2);
    }

    @Test
    public void updateSynsetByRemovingEdge() throws Exception {
        BabelNetSynset originSynset = new BabelNetSynset("Foo");
        String originSynsetId = "bn:00024922n";
        originSynset.setId(originSynsetId);

        BabelNetSynset pointedSynset = new BabelNetSynset("Bar");
        String pointedSynsetId = "bn:00024923n";
        pointedSynset.setId(pointedSynsetId);

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), Edge.RelationType.HOLONYM, 0.0);

        BabelNetSynset updatedOriginSynset = getMockOriginBabelNetSynsetWithoutEdges(originSynsetId);
        BabelNetSynset updatedPointedSynset = getMockPointedBabelNetSynset(pointedSynsetId);

        BabelNetManager mockManager = Mockito.mock(BabelNetManager.class);
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(updatedPointedSynset);

        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 0);
    }

    @Test
    public void updateSynsetByAddingEdgeThatWasRemoved() throws Exception {
        BabelNetSynset originalSynset = new BabelNetSynset("Foo");
        String originSynsetId = "bn:00024922n";
        originalSynset.setId(originSynsetId);

        BabelNetSynset pointedSynset = new BabelNetSynset("Bar");
        String pointedSynsetId = "bn:00024923n";
        pointedSynset.setId(pointedSynsetId);

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originalSynset.getId(), Edge.RelationType.HOLONYM, 0.0);

        BabelNetSynset updatedOriginSynset = getMockOriginBabelNetSynsetWithoutEdges(originSynsetId);
        BabelNetSynset updatedPointedSynset = getMockPointedBabelNetSynset(pointedSynsetId);

        BabelNetManager mockManager = Mockito.mock(BabelNetManager.class);
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(updatedPointedSynset);

        Database database = DatabaseTest.createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originalSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();
        board.removeEdge(edge.getId());
        board.save();

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 0);

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 0);
    }

    private BabelSense getMockOriginBabelSense() {
        BabelSense mockBabelSense = Mockito.mock(BabelSense.class);
        when(mockBabelSense.getSenseString()).thenReturn("Foo");
        return mockBabelSense;
    }

    private BabelSense getMockPointedBabelSense() {
        BabelSense mockBabelSense = Mockito.mock(BabelSense.class);
        when(mockBabelSense.getSenseString()).thenReturn("Bar");
        return mockBabelSense;
    }

    private BabelNetSynset getMockOriginBabelNetSynset(String originSynsetId, String pointedSynsetId) throws InvalidBabelSynsetIDException {
        BabelSense mockOriginBabelSense = getMockOriginBabelSense();

        BabelPointer mockBabelPointer = Mockito.mock(BabelPointer.class);
        when(mockBabelPointer.getName()).thenReturn("Edge description");
        when(mockBabelPointer.getRelationGroup()).thenReturn(BabelPointer.RelationGroup.OTHER);

        BabelSynsetIDRelation relation = new BabelSynsetIDRelation(null, mockBabelPointer, pointedSynsetId);

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockOriginBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(originSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(Arrays.asList(relation));

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelNetSynset getMockOriginBabelNetSynsetWithoutEdges(String originSynsetId) throws InvalidBabelSynsetIDException {
        BabelSense mockOriginBabelSense = getMockOriginBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockOriginBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(originSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelNetSynset getMockPointedBabelNetSynset(String pointedSynsetId) throws InvalidBabelSynsetIDException {
        BabelSense mockPointedBabelSense = getMockPointedBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockPointedBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(pointedSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }
}