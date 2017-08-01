package com.github.semres.gui;

import com.github.semres.*;
import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import it.uniroma1.lcl.babelnet.*;
import javafx.stage.Stage;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class SimpleUpdateTestApplication extends Main {
    private String originSynsetId = "bn:00024922n";
    private String pointedRemovedSynsetId = "bn:00024923n";
    private String pointedEditedSynsetId = "bn:00024924n";
    private String pointedAddedSynsetId = "bn:00024925n";

    private RelationType holonym = new BabelNetManager().getRelationTypes().get(0);
    private RelationType hypernym = new BabelNetManager().getRelationTypes().get(1);

    @Override
    public void start(Stage primaryStage) throws IOException {
        super.start(primaryStage);
        try {
            getMainController().setBabelNetManager(createMockBabelNetManager());
            getMainController().setDatabasesManager(createMockDatabasesManager());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private DatabasesManager createMockDatabasesManager() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        DatabasesManager mockManager = Mockito.mock(DatabasesManager.class);

        Set<String> databaseIds = new HashSet<>();
        databaseIds.add("Test database");
        when(mockManager.getRepositoryIDs()).thenReturn(databaseIds);
        when(mockManager.getBoard("Test database")).thenReturn(new Board(createTestDatabase(), getMainController().getBabelNetManager()));

        return mockManager;
    }

    private BabelNetManager createMockBabelNetManager() throws IOException, InvalidBabelSynsetIDException {
        BabelNetManager mockManager = Mockito.mock(BabelNetManager.class);

        BabelNetSynset originBabelNetSynset = new BabelNetSynset("Origin", originSynsetId);
        BabelNetSynset pointedAddedBabelNetSynset = new BabelNetSynset("Pointed added", pointedAddedSynsetId);
        BabelNetSynset pointedEditedBabelNetSynset = new BabelNetSynset("Pointed after edit", pointedEditedSynsetId);
        BabelNetSynset pointedRemovedBabelNetSynset = new BabelNetSynset("Pointed removed", pointedRemovedSynsetId);

        when(mockManager.getSynset(originSynsetId)).thenReturn(originBabelNetSynset);
        when(mockManager.getSynset(pointedAddedSynsetId)).thenReturn(pointedAddedBabelNetSynset);
        when(mockManager.getSynset(pointedEditedSynsetId)).thenReturn(pointedEditedBabelNetSynset);
        when(mockManager.getSynset(pointedRemovedSynsetId)).thenReturn(pointedRemovedBabelNetSynset);

        doAnswer(invocation -> {
            BabelNetEdge addedEdge = new BabelNetEdge(pointedAddedSynsetId, originSynsetId, "Added edge", holonym, 1);
            BabelNetEdge editedEdge = new BabelNetEdge(pointedEditedSynsetId, originSynsetId, "Edited edge", hypernym, 1);
            Map<String, Edge> edges = new HashMap<>();
            edges.put(addedEdge.getId(), addedEdge);
            edges.put(editedEdge.getId(), editedEdge);

            // Use reflection to add edges. It's necessary because method setOutgoingEdges i package-private.
            Field outgoingEdges = originBabelNetSynset.getClass().getSuperclass().getDeclaredField("outgoingEdges");
            outgoingEdges.setAccessible(true);
            outgoingEdges.set(originBabelNetSynset, edges);
            Field downloadedWithEdges = originBabelNetSynset.getClass().getDeclaredField("downloadedWithEdges");
            downloadedWithEdges.setAccessible(true);
            downloadedWithEdges.set(originBabelNetSynset, true);
            return null;
        }).when(mockManager).loadEdges(originBabelNetSynset);

        return mockManager;
    }

    private Database createTestDatabase() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Database database = com.github.semres.Utils.createTestDatabase();

        BabelNetSynset originSynset = new BabelNetSynset("Origin", originSynsetId, true);
        BabelNetSynset pointedRemoved = new BabelNetSynset("Pointed removed", pointedRemovedSynsetId);
        BabelNetSynset pointedEdited  = new BabelNetSynset("Pointed before edit", pointedEditedSynsetId);

        BabelNetEdge edgeToRemove = new BabelNetEdge(pointedRemoved.getId(), originSynset.getId(), holonym, 1.0);
        BabelNetEdge edgeToEdit = new BabelNetEdge(pointedEdited.getId(), originSynset.getId(), holonym, 1.0);

        database.addSynset(originSynset);
        database.addSynset(pointedRemoved);
        database.addSynset(pointedEdited);
        database.addEdge(edgeToRemove);
        database.addEdge(edgeToEdit);

        return database;
    }
}
