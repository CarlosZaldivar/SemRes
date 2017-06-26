package com.github.semres.gui;

import com.github.semres.Board;
import com.github.semres.Database;
import com.github.semres.DatabasesManager;
import com.github.semres.RelationType;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.user.UserEdge;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;
import javafx.stage.Stage;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UpdateWithEdgeMergeTestApplication extends Main {
    private String originSynsetId = "bn:00024922n";
    private String firstPointedSynsetId = "bn:00024923n";
    private String secondPointedSynsetId = "bn:00024924n";

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

        BabelNetSynset originSynset = getOriginBabelNetSynset();
        BabelNetSynset firstPointedSynset = getFirstPointedSynset();
        BabelNetSynset secondPointedSynset = getSecondPointedSynset();
        when(mockManager.getSynset(originSynsetId)).thenReturn(originSynset);
        when(mockManager.getSynset(firstPointedSynsetId)).thenReturn(firstPointedSynset);
        when(mockManager.getSynset(secondPointedSynsetId)).thenReturn(secondPointedSynset);
        return mockManager;
    }

    private Database createTestDatabase() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Database database = com.github.semres.Utils.createTestDatabase();

        BabelNetSynset originSynset = new BabelNetSynset("Origin");
        originSynset.setDownloadedWithEdges(true);
        originSynset.setId(originSynsetId);
        BabelNetSynset firstPointedSynset = new BabelNetSynset("First pointed");
        firstPointedSynset.setId(firstPointedSynsetId);
        BabelNetSynset secondPointedSynset = new BabelNetSynset("Second pointed");
        secondPointedSynset.setId(secondPointedSynsetId);

        RelationType userRelationType = new RelationType("Custom relation", "User");
        database.addRelationType(userRelationType);

        UserEdge firstUserEdge = new UserEdge(firstPointedSynsetId, originSynsetId, "User description", userRelationType, 1);
        UserEdge secondUserEdge = new UserEdge(secondPointedSynsetId, originSynsetId, new BabelNetManager().getRelationTypes().get(1), 0);

        database.addSynset(originSynset);
        database.addSynset(firstPointedSynset);
        database.addSynset(secondPointedSynset);
        database.addEdge(firstUserEdge);
        database.addEdge(secondUserEdge);

        return database;
    }

    private BabelNetSynset getOriginBabelNetSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockOriginBabelSense = getMockOriginBabelSense();

        BabelPointer mockFirstAddedPointer = Mockito.mock(BabelPointer.class);
        when(mockFirstAddedPointer.getName()).thenReturn("BabelNet description");
        when(mockFirstAddedPointer.getRelationGroup()).thenReturn(BabelPointer.RelationGroup.OTHER);
        BabelSynsetIDRelation firstAddedRelation = new BabelSynsetIDRelation(null, mockFirstAddedPointer, firstPointedSynsetId);

        BabelPointer mockSecondAddedPointer = Mockito.mock(BabelPointer.class);
        when(mockSecondAddedPointer.getName()).thenReturn("BabelNet description");
        when(mockSecondAddedPointer.getRelationGroup()).thenReturn(BabelPointer.RelationGroup.OTHER);
        BabelSynsetIDRelation secondAddedRelation = new BabelSynsetIDRelation(null, mockSecondAddedPointer, secondPointedSynsetId);


        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockOriginBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(originSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(Arrays.asList(firstAddedRelation, secondAddedRelation));

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelNetSynset getFirstPointedSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockPointedBabelSense = getMockFirstPointedBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockPointedBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(firstPointedSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelNetSynset getSecondPointedSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockPointedBabelSense = getMockSecondPointedBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockPointedBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(secondPointedSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelSense getMockFirstPointedBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("First pointed");
        return babelSense;
    }

    private BabelSense getMockSecondPointedBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Second pointed");
        return babelSense;
    }

    private BabelSense getMockOriginBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Origin");
        return babelSense;
    }
}
