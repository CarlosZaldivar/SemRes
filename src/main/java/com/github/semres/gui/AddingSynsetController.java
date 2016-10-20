package com.github.semres.gui;

import com.github.semres.user.UserSynset;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
            UserSynset newSynset = new UserSynset(representation);
            newSynset.setDescription(description);
            ((MainController) parent).addSynset(newSynset);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.close();
        }
    }
}
