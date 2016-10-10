package com.github.semres.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddingDatabaseController extends ChildController {
    @FXML
    private TextField newDatabaseNameTF;
    @FXML
    private Button createButton;

    public void createNewDatabase() {
        try {
            String newDatabaseName = newDatabaseNameTF.getText();
            ((DatabasesController)parent).addDatabase(newDatabaseName);
        } catch (IllegalArgumentException e) {
            System.out.println("illegal argument");
        }
    }
}
