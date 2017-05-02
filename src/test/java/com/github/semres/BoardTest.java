package com.github.semres;


import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.gui.IDAlreadyTakenException;
import com.github.semres.user.UserEdge;
import com.github.semres.user.UserSynset;
import org.junit.Test;

import static org.junit.Assert.*;

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
}