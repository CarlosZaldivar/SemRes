package com.github.semres.gui;

import com.github.semres.babelnet.BabelNetManager;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditApiKeyController extends ChildController implements Initializable {
    @FXML private TextField apiKeyTF;
    @FXML private Button editButton;
    @FXML private Button cancelButton;
    @FXML private Button okButton;

    private BooleanProperty editing = new SimpleBooleanProperty(false);
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okButton.visibleProperty().bind(editing);
        okButton.managedProperty().bind(okButton.visibleProperty());
        cancelButton.visibleProperty().bind(editing);
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        editButton.visibleProperty().bind(editing.not());
        editButton.managedProperty().bind(editButton.visibleProperty());

        apiKeyTF.editableProperty().bind(editing);
        Platform.runLater(() -> apiKeyTF.getParent().requestFocus());
    }

    public void startEditing() {
        editing.set(true);
    }

    public void cancelEditing() {
        editing.set(false);
        apiKeyTF.setText(BabelNetManager.getApiKey());
    }

    public void save() throws IOException {
        mainController.setBabelNetApiKey(apiKeyTF.getText() != null ? apiKeyTF.getText() : "");
        Utils.showInfo("Changes will be applied after the next program launch.");
        Stage stage = (Stage) apiKeyTF.getScene().getWindow();
        stage.close();
    }

    @Override
    public void setParent(Controller parent) {
        mainController = (MainController) parent;
        if (mainController.getNewApiKey() == null) {
            apiKeyTF.setText(BabelNetManager.getApiKey());
        } else {
            apiKeyTF.setText(mainController.getNewApiKey());
        }
    }
}
