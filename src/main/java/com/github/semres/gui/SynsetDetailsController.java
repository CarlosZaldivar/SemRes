package com.github.semres.gui;

import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.user.UserSynset;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SynsetDetailsController extends ChildController implements Initializable {
    @FXML private TextField idTF;
    @FXML private TextField representationTF;
    @FXML private TextArea descriptionTA;
    @FXML private ButtonBar buttonBar;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private BooleanProperty editing = new SimpleBooleanProperty(false);
    private Synset synset;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.visibleProperty().bind(editing);
        saveButton.managedProperty().bind(saveButton.visibleProperty());
        cancelButton.visibleProperty().bind(editing);
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        editButton.visibleProperty().bind(editing.not());
        editButton.managedProperty().bind(editButton.visibleProperty());

        representationTF.editableProperty().bind(editing);
        descriptionTA.editableProperty().bind(editing);

        BooleanBinding representationValid = Bindings.createBooleanBinding(() -> representationTF.getText().length() > 0, representationTF.textProperty());
        saveButton.disableProperty().bind(representationValid.not());
    }

    public void setSynset(Synset synset) {
        this.synset = synset;
        idTF.textProperty().set(synset.getId());
        representationTF.textProperty().set(synset.getRepresentation());
        descriptionTA.textProperty().set(synset.getDescription());

        if (synset instanceof BabelNetSynset) {
            buttonBar.setManaged(false);
            buttonBar.setVisible(false);
        }
    }

    public void startEditing() {
        editing.set(true);
    }

    public void cancelEditing() {
        editing.set(false);
        representationTF.textProperty().set(synset.getRepresentation());
        descriptionTA.textProperty().set(synset.getDescription());
    }

    public void save() {
        UserSynset originalSynset = (UserSynset) synset;
        UserSynset editedSynset = originalSynset.changeRepresentation(representationTF.getText());
        editedSynset = editedSynset.changeDescription(descriptionTA.getText());
        ((MainController) parent).editSynset(originalSynset, editedSynset);
        editing.set(false);
    }
}
