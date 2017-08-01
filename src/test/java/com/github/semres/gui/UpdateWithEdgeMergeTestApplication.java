package com.github.semres.gui;

import com.github.semres.*;
import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.user.UserEdge;
import it.uniroma1.lcl.babelnet.*;
import javafx.stage.Stage;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class UpdateWithEdgeMergeTestApplication extends Main {
    private String originSynsetId = "bn:00024922n";
    private String firstPointedSynsetId = "bn:00024923n";
    private String secondPointedSynsetId = "bn:00024924n";

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

        BabelNetSynset originSynset = new BabelNetSynset("Origin", originSynsetId);
        BabelNetSynset firstPointedSynset = new BabelNetSynset("First pointed", firstPointedSynsetId);
        BabelNetSynset secondPointedSynset = new BabelNetSynset("Second pointed", secondPointedSynsetId);

        when(mockManager.getSynset(originSynsetId)).thenReturn(originSynset);
        when(mockManager.getSynset(firstPointedSynsetId)).thenReturn(firstPointedSynset);
        when(mockManager.getSynset(secondPointedSynsetId)).thenReturn(secondPointedSynset);

        doAnswer(invocation -> {
            BabelNetEdge firstEdge = new BabelNetEdge(firstPointedSynsetId, originSynsetId, "BabelNet description", hypernym, 1);
            BabelNetEdge secondEdge = new BabelNetEdge(secondPointedSynsetId, originSynsetId, "BabelNet description", hypernym, 1);
            Map<String, Edge> edges = new HashMap<>();
            edges.put(firstEdge.getId(), firstEdge);
            edges.put(secondEdge.getId(), secondEdge);

            // Use reflection to add edges. It's necessary because method setOutgoingEdges i package-private.
            Field outgoingEdges = originSynset.getClass().getSuperclass().getDeclaredField("outgoingEdges");
            outgoingEdges.setAccessible(true);
            outgoingEdges.set(originSynset, edges);
            Field downloadedWithEdges = originSynset.getClass().getDeclaredField("downloadedWithEdges");
            downloadedWithEdges.setAccessible(true);
            downloadedWithEdges.set(originSynset, true);
            return null;
        }).when(mockManager).loadEdges(originSynset);

        return mockManager;
    }

    private Database createTestDatabase() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Database database = com.github.semres.Utils.createTestDatabase();

        BabelNetSynset originSynset = new BabelNetSynset("Origin", originSynsetId, null, true);
        BabelNetSynset firstPointedSynset = new BabelNetSynset("First pointed", firstPointedSynsetId);
        BabelNetSynset secondPointedSynset = new BabelNetSynset("Second pointed", secondPointedSynsetId);

        RelationType userRelationType = new RelationType("Custom relation", "User");
        database.addRelationType(userRelationType);

        UserEdge firstUserEdge = new UserEdge(firstPointedSynsetId, originSynsetId, "User description", userRelationType, 1);
        UserEdge secondUserEdge = new UserEdge(secondPointedSynsetId, originSynsetId, holonym, 0);

        database.addSynset(originSynset);
        database.addSynset(firstPointedSynset);
        database.addSynset(secondPointedSynset);
        database.addEdge(firstUserEdge);
        database.addEdge(secondUserEdge);

        return database;
    }
}
