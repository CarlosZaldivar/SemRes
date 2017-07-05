package com.github.semres.gui;

import com.github.semres.*;
import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.jlt.util.Language;
import javafx.stage.Stage;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
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

        BabelNetSynset originBabelNetSynset = getOriginBabelNetSynset();
        when(mockManager.getSynset(originSynsetId)).thenReturn(originBabelNetSynset);
        when(mockManager.getSynset(pointedSynsetId)).thenReturn(null);
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

    private BabelNetSynset getOriginBabelNetSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockOriginBabelSense = getMockOriginBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockOriginBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(originSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelSense getMockOriginBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Origin");
        return babelSense;
    }
}
