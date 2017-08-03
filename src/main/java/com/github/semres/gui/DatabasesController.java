package com.github.semres.gui;

import com.github.semres.Board;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class DatabasesController extends ChildController {
    @FXML private ListView<String> listView;
    @FXML private Button addButton;
    @FXML private Button deleteButton;

    private final ObservableList<String> observableList = FXCollections.observableArrayList();
    
    private MainController mainController;

    public void openNewDatabaseDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/fxml/add-database.fxml")));
        Parent root = loader.load();
        Stage databasesStage = new Stage();
        databasesStage.setTitle("Add database");
        databasesStage.setScene(new Scene(root));
        databasesStage.sizeToScene();
        databasesStage.initOwner(addButton.getScene().getWindow());
        databasesStage.initModality(Modality.WINDOW_MODAL);

        AddingDatabaseController childController = loader.getController();
        childController.setParent(this);

        databasesStage.show();
    }

    void addDatabase(String name, String baseIri) {
        mainController.getDatabasesManager().addRepository(name, baseIri);
        observableList.add(name);
    }

    public void deleteDatabase() {
        String name = listView.getSelectionModel().getSelectedItem();
        if (name != null && !name.equals("SYSTEM")) {
            mainController.getDatabasesManager().deleteRepository(name);
            observableList.remove(name);
        }
    }

    public void clickedListView(MouseEvent click) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (click.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
            String repositoryName = listView.getSelectionModel().getSelectedItem();
            Board loadedBoard =  mainController.getDatabasesManager().getBoard(repositoryName);
            mainController.setBoard(loadedBoard);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.close();
        }
    }

    @Override
    public void setParent(JavaFXController parent) {
        mainController = (MainController) parent;
        Set<String> databasesNames = mainController.getDatabasesManager().getRepositoryIDs();
        databasesNames.remove("SYSTEM");

        observableList.setAll(databasesNames);
        listView.setItems(observableList);
    }
}
