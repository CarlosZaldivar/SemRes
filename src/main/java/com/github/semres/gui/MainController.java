package com.github.semres.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML
    private MenuBar menuBar;

    public void openDatabasesWindow() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/databases-list.fxml"));
        Stage databasesStage = new Stage();
        databasesStage.setTitle("Databases");
        databasesStage.setScene(new Scene(root, 300, 275));
        databasesStage.initOwner(menuBar.getScene().getWindow());
        databasesStage.initModality(Modality.WINDOW_MODAL);
        databasesStage.show();
    }
}
