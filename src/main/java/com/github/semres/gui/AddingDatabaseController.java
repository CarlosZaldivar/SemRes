package com.github.semres.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddingDatabaseController extends ChildController {
    @FXML private TextField newDatabaseNameTF;
    @FXML private Button createButton;

    private DatabasesController databasesController;

    @Override
    public void setParent(Controller parent) {
        databasesController = (DatabasesController) parent;
    }

    public void createNewDatabase() {
        try {
            String newDatabaseName = newDatabaseNameTF.getText();
            databasesController.addDatabase(newDatabaseName);
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.close();
        } catch (IllegalArgumentException e) {
            Utils.showError("Database name already taken.");
        }
    }
}
