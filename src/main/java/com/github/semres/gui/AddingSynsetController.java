package com.github.semres.gui;

import com.github.semres.user.UserSynset;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddingSynsetController extends ChildController implements Initializable {
    @FXML
    private
    TextField representationTF;
    @FXML
    private
    TextArea descriptionTA;
    @FXML
    private
    Button addButton;

    public void addSynset() {
        String representation = representationTF.getText();
        String description = descriptionTA.getText();

        UserSynset newSynset = new UserSynset(representation);

        if (!description.isEmpty()) {
            newSynset = newSynset.changeDescription(description);
        }
        ((MainController) parent).addSynset(newSynset);
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BooleanBinding representationValid = Bindings.createBooleanBinding(() -> representationTF.getText().length() > 0, representationTF.textProperty());
        addButton.disableProperty().bind(representationValid.not());
    }
}
