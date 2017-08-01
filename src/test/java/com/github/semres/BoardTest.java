package com.github.semres;


import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.gui.IDAlreadyTakenException;
import com.github.semres.user.UserEdge;
import com.github.semres.user.UserSynset;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static com.github.semres.Utils.createTestDatabase;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoardTest {
    private RelationType holonym = new BabelNetManager().getRelationTypes().stream().filter(r -> r.getType().equals("HOLONYM")).findFirst().get();
    private RelationType meronym = new BabelNetManager().getRelationTypes().stream().filter(r -> r.getType().equals("MERONYM")).findFirst().get();

    @Test
    public void createSynset() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        board.createSynset("Foo", null);
        board.save();

        assertTrue(database.searchSynsets("Foo").size() == 1);
    }

    @Test
    public void addSynset() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo", "123");
        board.addSynset(synset);
        board.save();

        assertTrue(database.searchSynsets("Foo").size() == 1);
    }

    @Test
    public void removeSynset() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo", "123");
        board.addSynset(synset);
        board.save();
        board.removeSynset("123");
        board.save();
        assertTrue(database.searchSynsets("Foo").size() == 0);
    }

    @Test
    public void removeEdge() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        Synset originSynset = new UserSynset("Foo", "123");
        Synset pointedSynset = new UserSynset("Bar", "124");

        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), holonym, 0);
        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();
        board.removeEdge("123-124");
        board.save();

        Synset loadedSynset = database.searchSynsets("Foo").get(0);
        database.loadEdges(loadedSynset);

        assertTrue(loadedSynset.getOutgoingEdges().size() == 0);
    }

    @Test
    public void addRemovedBabelnetEdge() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        Synset originSynset = new BabelNetSynset("Foo", "bn:00024922n");
        Synset pointedSynset = new BabelNetSynset("Bar", "bn:00024923n");

        Edge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), holonym, 0);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();
        board.removeEdge("bn:00024922n-bn:00024923n");
        board.save();
        // Add removed edge again
        try {
            board.addEdge(edge);
            throw new RuntimeException();
        } catch (RuntimeException e) {}
        board.save();

        Synset loadedSynset = database.searchSynsets("Foo").get(0);
        database.loadEdges(loadedSynset);

        assertTrue(loadedSynset.getOutgoingEdges().size() == 0);
    }

    @Test
    public void addSameSynsetMultipleTimes() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        Synset synset = new BabelNetSynset("Foo", "bn:00024922n");
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
        Database database = createTestDatabase();
        Board board = new Board(database);


        Synset originSynset = new BabelNetSynset("Foo", "bn:00024922n");
        Synset pointedSynset = new BabelNetSynset("Bar", "bn:00024923n");

        Edge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), holonym, 1);

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
        Database database = createTestDatabase();
        Board board = new Board(database);

        UserSynset synset = new UserSynset("Foo", "123", "Bar");
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
        Database database = createTestDatabase();
        Board board = new Board(database);

        UserSynset originSynset = board.createSynset("Foo");
        UserSynset pointedSynset = board.createSynset("Bar");

        UserEdge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), "Description 1", holonym, 0);
        board.addEdge(edge);
        board.save();

        UserEdge savedEdge = (UserEdge) database.getOutgoingEdges(originSynset).get(0);
        assertTrue(savedEdge.getRelationType().equals(holonym));
        assertTrue(savedEdge.getDescription().equals("Description 1"));
        assertTrue(savedEdge.getWeight() == 0);

        UserEdge editedEdge = edge.changeRelationType(meronym).changeWeight(1).changeDescription("Description 2");
        board.editEdge(edge, editedEdge);
        assertTrue(board.getEdge(editedEdge.getId()) == editedEdge);
        board.save();
        assertTrue(database.getOutgoingEdges(originSynset).size() == 1);

        savedEdge = (UserEdge) database.getOutgoingEdges(originSynset).get(0);
        assertTrue(savedEdge.relationType.equals(meronym));
        assertTrue(savedEdge.getDescription().equals("Description 2"));
        assertTrue(savedEdge.getWeight() == 1);
    }


    @Test
    public void update() throws Exception {
        String synsetId = "bn:00024922n";
        BabelNetSynset originalSynset = new BabelNetSynset("Foo", synsetId, "Description 1");
        BabelNetSynset updatedSynset = new BabelNetSynset("Bar", synsetId, "Description 2");

        BabelNetManager babelNetManager = mock(BabelNetManager.class);
        when(babelNetManager.getSynset(synsetId)).thenReturn(updatedSynset);

        Database database = createTestDatabase();
        Board board = new Board(database, babelNetManager);

        board.addSynset(originalSynset);
        board.save();

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(synsetId).getRepresentation().equals("Bar"));

        BabelNetSynset savedSynset = (BabelNetSynset) database.getSynset(synsetId);
        assertTrue(savedSynset.getRepresentation().equals("Bar"));
        assertTrue(savedSynset.getDescription().equals("Description 2"));
    }

    @Test
    public void updateSynsetByAddingEdge() throws Exception {
        String originSynsetId = "bn:00024922n";
        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId, null, true);
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset(pointedSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithEdge(updatedOriginSynset, updatedPointedSynset);

        Database database = createTestDatabase();
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
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId, null, true);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        BabelNetManager mockManager = createMockBabelNetManagerWithEdge(updatedOriginSynset, updatedPointedSynset);

        Database database = createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.save();
        assertTrue(database.searchSynsets("").size() == 1);

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);
        board.save();
        assertTrue(database.searchSynsets("").size() == 2);
    }

    @Test
    public void updateSynsetByRemovingEdge() throws Exception {
        String originSynsetId = "bn:00024922n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId, true);

        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), holonym, 0.0);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset(pointedSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithoutEdge(updatedOriginSynset, updatedPointedSynset);

        Database database = createTestDatabase();
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
        String originSynsetId = "bn:00024922n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId);

        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), holonym, 0.0);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset(pointedSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithEdge(updatedOriginSynset, updatedPointedSynset);

        Database database = createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();
        board.removeEdge(edge.getId());
        board.save();

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 0);

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 0);
    }

    @Test
    public void updateSynsetWithDuplicateEdges() throws Exception {
        String originSynsetId = "bn:00024922n";
        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId, true);
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        UserEdge userEdge = new UserEdge(pointedSynsetId, originSynsetId, holonym, 1);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset(pointedSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithEdge(updatedOriginSynset, updatedPointedSynset);

        Database database = createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(userEdge);
        board.save();

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);
        assertTrue(board.getEdge(userEdge.getId()) instanceof BabelNetEdge);
    }

    @Test
    public void cancelEdgeReplacement() throws Exception {
        String originSynsetId = "bn:00024922n";
        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId, true);
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        UserEdge userEdge = new UserEdge(pointedSynsetId, originSynsetId, holonym, 1);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset(pointedSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithEdge(updatedOriginSynset, updatedPointedSynset);

        Database database = createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(userEdge);
        board.save();

        List<SynsetUpdate> updates = board.checkForUpdates();
        SynsetUpdate update = updates.stream().filter(s -> s.getOriginalSynset().getId().equals(originSynsetId)).findFirst().get();
        update.cancelEdgeReplacement(userEdge.getId());
        board.update(updates);

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);
        assertTrue(board.getEdge(userEdge.getId()) instanceof UserEdge);
    }

    @Test
    public void mergeEdgeDescription() throws Exception {
        String originSynsetId = "bn:00024922n";
        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset originSynset = new BabelNetSynset("Foo", originSynsetId, true);
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        UserEdge userEdge = new UserEdge(pointedSynsetId, originSynsetId, "Description 1", holonym, 1);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originSynset);
        BabelNetSynset updatedPointedSynset = new BabelNetSynset(pointedSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithEdge(updatedOriginSynset, updatedPointedSynset, "Description 2");

        Database database = createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(userEdge);
        board.save();

        List<SynsetUpdate> updates = board.checkForUpdates();
        SynsetUpdate update = updates.stream().filter(s -> s.getOriginalSynset().getId().equals(originSynsetId)).findFirst().get();
        update.mergeEdgeDescriptions(userEdge.getId());
        board.update(updates);

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 1);
        assertTrue(board.getEdge(userEdge.getId()).getDescription().equals(String.format("Description 2%n---%nDescription 1")));
    }

    @Test
    public void removeSynsetInAnUpdate() throws Exception {
        String originSynsetId = "bn:00024922n";
        BabelNetSynset originalSynset = new BabelNetSynset("Foo", originSynsetId, null, true);

        String pointedSynsetId = "bn:00024923n";
        BabelNetSynset pointedSynset = new BabelNetSynset("Bar", pointedSynsetId);

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originalSynset.getId(), holonym, 0);

        BabelNetSynset updatedOriginSynset = new BabelNetSynset(originalSynset);

        BabelNetManager mockManager = createMockBabelNetManagerWithoutPointedSynset(updatedOriginSynset);

        Database database = createTestDatabase();
        Board board = new Board(database, mockManager);

        board.addSynset(originalSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);
        board.save();

        assertTrue(board.getSynsets().size() == 2);
        assertTrue(database.getSynset(pointedSynsetId) != null);

        board.update(board.checkForUpdates());

        assertTrue(board.getSynset(originSynsetId).getOutgoingEdges().size() == 0);
        assertTrue(board.getSynsets().size() == 1);
        assertTrue(database.getSynset(pointedSynsetId) == null);
    }

    @Test(expected = RelationTypeInUseException.class)
    public void removeUsedRelationType() throws Exception {
        Database database = createTestDatabase();
        Board board = new Board(database);

        Synset originSynset = new UserSynset("Foo", "123");
        Synset pointedSynset = new UserSynset("Bar", "124");

        RelationType oldRelationType = new RelationType("RelationX", "User");
        UserEdge edge = new UserEdge(pointedSynset.getId(), originSynset.getId(), oldRelationType, 0);
        board.addSynset(originSynset);
        board.addSynset(pointedSynset);
        board.addEdge(edge);

        try {
            board.removeRelationType(oldRelationType);
            throw new RuntimeException("RelationTypeInUseException not thrown.");
        } catch (RelationTypeInUseException e) {}

        board.save();

        RelationType newRelationType = new RelationType("RelationY", "User");
        UserEdge editedEdge = edge.changeRelationType(newRelationType);
        board.editEdge(edge, editedEdge);
        board.removeRelationType(newRelationType);
    }

    private BabelNetManager createMockBabelNetManagerWithEdge(BabelNetSynset updatedOriginSynset, BabelNetSynset updatedPointedSynset) throws IOException {
        return createMockBabelNetManagerWithEdge(updatedOriginSynset,updatedPointedSynset, "Edge description");
    }

    private BabelNetManager createMockBabelNetManagerWithEdge(
            BabelNetSynset updatedOriginSynset, BabelNetSynset updatedPointedSynset, String edgeDescription) throws IOException {
        BabelNetManager mockManager = mock(BabelNetManager.class);
        String originSynsetId = updatedOriginSynset.getId();
        String pointedSynsetId = updatedPointedSynset.getId();
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(updatedPointedSynset);
        doAnswer(invocation -> {
            BabelNetEdge edge = new BabelNetEdge(pointedSynsetId, originSynsetId, edgeDescription, holonym, 1);
            Map<String, Edge> edges = new HashMap<>();
            edges.put(edge.getId(), edge);

            // Use reflection to add edges. It's necessary because method setOutgoingEdges i package-private.
            Field outgoingEdges = updatedOriginSynset.getClass().getSuperclass().getDeclaredField("outgoingEdges");
            outgoingEdges.setAccessible(true);
            outgoingEdges.set(updatedOriginSynset, edges);
            Field downloadedWithEdges = updatedOriginSynset.getClass().getDeclaredField("downloadedWithEdges");
            downloadedWithEdges.setAccessible(true);
            downloadedWithEdges.set(updatedOriginSynset, true);
            return null;
        }).when(mockManager).loadEdges(updatedOriginSynset);
        return mockManager;
    }

    private BabelNetManager createMockBabelNetManagerWithoutEdge(
            BabelNetSynset updatedOriginSynset, BabelNetSynset updatedPointedSynset) throws IOException {
        BabelNetManager mockManager = mock(BabelNetManager.class);
        String originSynsetId = updatedOriginSynset.getId();
        String pointedSynsetId = updatedPointedSynset.getId();
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(updatedPointedSynset);
        doAnswer(invocation -> {
            Field downloadedWithEdges = updatedOriginSynset.getClass().getDeclaredField("downloadedWithEdges");
            downloadedWithEdges.setAccessible(true);
            downloadedWithEdges.set(updatedOriginSynset, true);
            return null;
        }).when(mockManager).loadEdges(updatedOriginSynset);
        return mockManager;
    }

    private BabelNetManager createMockBabelNetManagerWithoutPointedSynset(BabelNetSynset updatedOriginSynset) throws IOException {
        BabelNetManager mockManager = mock(BabelNetManager.class);
        String originSynsetId = updatedOriginSynset.getId();
        when(mockManager.getSynset(originSynsetId)).thenReturn(updatedOriginSynset);
        doAnswer(invocation -> {
            Field downloadedWithEdges = updatedOriginSynset.getClass().getDeclaredField("downloadedWithEdges");
            downloadedWithEdges.setAccessible(true);
            downloadedWithEdges.set(updatedOriginSynset, true);
            return null;
        }).when(mockManager).loadEdges(updatedOriginSynset);
        return mockManager;
    }
}