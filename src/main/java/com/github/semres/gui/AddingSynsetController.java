package com.github.semres.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddingSynsetController extends ChildController {
    @FXML
    TextField representationTF;
    @FXML
    TextArea descriptionTA;
    @FXML
    Button addButton;

    public void addSynset() {
        String representation = representationTF.getText();
        String description = descriptionTA.getText();

        if (representation != null && !representation.isEmpty()) {

        }
    }
}
