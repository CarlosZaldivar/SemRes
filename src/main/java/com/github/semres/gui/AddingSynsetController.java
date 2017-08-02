package com.github.semres.gui;

import com.github.semres.Synset;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddingSynsetController extends ChildController implements Initializable {
    @FXML private TextField representationTF;
    @FXML private TextArea descriptionTA;
    @FXML private Button addButton;
    private MainController mainController;

    public void addSynset() {
        String representation = representationTF.getText();
        String description = descriptionTA.getText();

        Synset newSynset = mainController.getBoard().createSynset(representation, description);
        mainController.getBrowserController().addSynset(newSynset);
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }
    
    @Override
    public void setParent(Controller parent) {
        mainController = (MainController) parent;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addButton.disableProperty().bind(representationTF.textProperty().isEmpty());
    }
}
