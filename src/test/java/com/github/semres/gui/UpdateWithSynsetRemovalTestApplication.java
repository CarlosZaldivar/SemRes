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

public class UpdateWithSynsetRemovalTestApplication extends Main  {
    private String originSynsetId = "bn:00024922n";
    private String pointedSynsetId = "bn:00024923n";

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
        when(mockManager.getSynset(originSynsetId)).thenReturn(originSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(null);

        doAnswer(invocation -> {
            Field downloadedWithEdges = originSynset.getClass().getDeclaredField("downloadedWithEdges");
            downloadedWithEdges.setAccessible(true);
            downloadedWithEdges.set(originSynset, true);
            return null;
        }).when(mockManager).loadEdges(originSynset);

        return mockManager;
    }

    private Database createTestDatabase() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Database database = com.github.semres.Utils.createTestDatabase();

        BabelNetSynset originSynset = new BabelNetSynset("Origin", originSynsetId, true);
        BabelNetSynset pointedSynset = new BabelNetSynset("Pointed removed", pointedSynsetId);

        BabelNetEdge edge = new BabelNetEdge(pointedSynset.getId(), originSynset.getId(), new BabelNetManager().getRelationTypes().get(0), 1.0);

        database.addSynset(originSynset);
        database.addSynset(pointedSynset);
        database.addEdge(edge);

        return database;
    }
}
