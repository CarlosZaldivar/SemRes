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
    @FXML
    private ListView<String> listView;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private final ObservableList<String> observableList = FXCollections.observableArrayList();

    public void openNewDatabaseDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/fxml/add-database.fxml")));
        Parent root = loader.load();
        Stage databasesStage = new Stage();
        databasesStage.setTitle("Add database");
        databasesStage.setScene(new Scene(root, 200, 150));
        databasesStage.initOwner(addButton.getScene().getWindow());
        databasesStage.initModality(Modality.WINDOW_MODAL);

        AddingDatabaseController childController = loader.getController();
        childController.setParent(this);

        databasesStage.show();
    }

    void addDatabase(String name) {
        ((MainController) parent).getDatabasesManager().addRepository(name);
        observableList.add(name);
    }

    public void deleteDatabase() {
        String name = listView.getSelectionModel().getSelectedItem();
        if (name != null && !name.equals("SYSTEM")) {
            ((MainController) parent).getDatabasesManager().deleteRepository(name);
            observableList.remove(name);
        }
    }

    public void clickedListView(MouseEvent click) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (click.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
            String repositoryName = listView.getSelectionModel().getSelectedItem();
            Board loadedBoard =  ((MainController) parent).getDatabasesManager().getBoard(repositoryName);
            ((MainController) parent).setBoard(loadedBoard);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.close();
        }
    }

    @Override
    public void setParent(Controller parent) {
        super.setParent(parent);
        Set<String> databasesNames = ((MainController) parent).getDatabasesManager().getRepositoryIDs();
        databasesNames.remove("SYSTEM");

        observableList.setAll(databasesNames);
        listView.setItems(observableList);
    }
}
