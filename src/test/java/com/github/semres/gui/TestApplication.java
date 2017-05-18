package com.github.semres.gui;

import com.github.semres.*;
import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;
import javafx.stage.Stage;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestApplication extends Main {
    String originSynsetId = "bn:00024922n";
    String pointedRemovedSynsetId = "bn:00024923n";
    String pointedEditedSynsetId = "bn:00024924n";
    String pointedAddedSynsetId = "bn:00024925n";

    @Override
    public void start(Stage primaryStage) throws Exception {
        super.start(primaryStage);
        getMainController().setBabelNetManager(createMockBabelNetManager());
        getMainController().setDatabasesManager(createMockDatabasesManager());
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
        BabelNetSynset pointedAddedBabelNetSynset = getPointedAddedBabelNetSynset();
        BabelNetSynset pointedEditedBabelNetSynset = getPointedEditedBabelNetSynset();
        BabelNetSynset pointedRemovedBabelNetSynset = getPointedRemovedBabelNetSynset();
        when(mockManager.getSynset(originSynsetId)).thenReturn(originBabelNetSynset);
        when(mockManager.getSynset(pointedAddedSynsetId)).thenReturn(pointedAddedBabelNetSynset);
        when(mockManager.getSynset(pointedEditedSynsetId)).thenReturn(pointedEditedBabelNetSynset);
        when(mockManager.getSynset(pointedRemovedSynsetId)).thenReturn(pointedRemovedBabelNetSynset);
        return mockManager;
    }

    private Database createTestDatabase() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Database database = DatabaseTest.createTestDatabase();

        BabelNetSynset originSynset = new BabelNetSynset("Origin");
        originSynset.setId(originSynsetId);
        BabelNetSynset pointedRemoved = new BabelNetSynset("Pointed removed");
        pointedRemoved.setId(pointedRemovedSynsetId);
        BabelNetSynset pointedEdited  = new BabelNetSynset("Pointed edited");
        pointedEdited.setId(pointedEditedSynsetId);

        BabelNetEdge edgeToRemove = new BabelNetEdge(pointedRemoved.getId(), originSynset.getId(), Edge.RelationType.OTHER, 1.0);
        BabelNetEdge edgeToEdit = new BabelNetEdge(pointedEdited.getId(), originSynset.getId(), Edge.RelationType.OTHER, 1.0);

        database.addSynset(originSynset);
        database.addSynset(pointedRemoved);
        database.addSynset(pointedEdited);
        database.addEdge(edgeToRemove);
        database.addEdge(edgeToEdit);

        return database;
    }

    private BabelNetSynset getOriginBabelNetSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockOriginBabelSense = getMockOriginBabelSense();

        BabelPointer mockRemovedPointer = Mockito.mock(BabelPointer.class);
        when(mockRemovedPointer.getName()).thenReturn("Added edge");
        when(mockRemovedPointer.getRelationGroup()).thenReturn(BabelPointer.RelationGroup.OTHER);
        BabelSynsetIDRelation addedRelation = new BabelSynsetIDRelation(null, mockRemovedPointer, pointedAddedSynsetId);

        BabelPointer mockEditedPointer = Mockito.mock(BabelPointer.class);
        when(mockEditedPointer.getName()).thenReturn("Edited edge");
        when(mockEditedPointer.getRelationGroup()).thenReturn(BabelPointer.RelationGroup.OTHER);
        BabelSynsetIDRelation editedRelation = new BabelSynsetIDRelation(null, mockEditedPointer, pointedEditedSynsetId);


        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockOriginBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(originSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(Arrays.asList(addedRelation, editedRelation));

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelNetSynset getPointedAddedBabelNetSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockPointedBabelSense = getMockPointedAddedBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockPointedBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(pointedAddedSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }


    private BabelNetSynset getPointedEditedBabelNetSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockPointedBabelSense = getMockPointedEditedBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockPointedBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(pointedEditedSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }


    private BabelNetSynset getPointedRemovedBabelNetSynset() throws InvalidBabelSynsetIDException {
        BabelSense mockPointedBabelSense = getMockPointedRemovedBabelSense();

        BabelSynset mockBabelSynset = Mockito.mock(BabelSynset.class);
        when(mockBabelSynset.getMainSense(any(Language.class))).thenReturn(mockPointedBabelSense);
        when(mockBabelSynset.getId()).thenReturn(new BabelSynsetID(pointedRemovedSynsetId));
        when(mockBabelSynset.getEdges()).thenReturn(new ArrayList<>());

        return new BabelNetSynset(mockBabelSynset);
    }

    private BabelSense getMockPointedAddedBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Pointed added");
        return babelSense;
    }

    private BabelSense getMockPointedEditedBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Pointed edited");
        return babelSense;
    }

    private BabelSense getMockPointedRemovedBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Pointed removed");
        return babelSense;
    }

    private BabelSense getMockOriginBabelSense() {
        BabelSense babelSense = Mockito.mock(BabelSense.class);
        when(babelSense.getSenseString()).thenReturn("Origin");
        return babelSense;
    }
}
