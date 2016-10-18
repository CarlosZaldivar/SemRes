package com.github.semres.gui;

import com.github.semres.SemRes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class DatabasesController extends Controller implements Initializable {
    @FXML
    private ListView listView;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList observableList = FXCollections.observableArrayList();
    private Set<String> databasesNames;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databasesNames = SemRes.getInstance().getRepositoryIDs();
        databasesNames.remove("SYSTEM");

        observableList.setAll(databasesNames);
        listView.setItems(observableList);
    }

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

    public void addDatabase(String name) {
        SemRes.getInstance().addRepository(name);
        observableList.add(name);
    }

    public void deleteDatabase() {
        String name = (String)listView.getSelectionModel().getSelectedItem();
        if (name != null) {
            SemRes.getInstance().deleteRepository(name);
            observableList.remove(name);
        }
    }
}