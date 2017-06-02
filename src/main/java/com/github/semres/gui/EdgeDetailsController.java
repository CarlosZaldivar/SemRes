package com.github.semres.gui;

import com.github.semres.Edge;
import com.github.semres.RelationType;
import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.user.UserEdge;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EdgeDetailsController extends EdgeController implements Initializable {
    @FXML private ComboBox<RelationType> relationTypeCB;
    @FXML private TextField weightTF;
    @FXML private TextArea descriptionTA;
    @FXML private Button editButton;
    @FXML private Button cancelButton;
    @FXML private Button okButton;
    @FXML private ButtonBar buttonBar;
    private BooleanProperty editing = new SimpleBooleanProperty(false);
    private Edge edge;

    @Override
    public void setParent(Controller parent) {
        super.setParent(parent);
        relationTypeCB.setItems(relationTypes);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okButton.disableProperty().bind(getWeightInvalidProperty(weightTF));

        okButton.visibleProperty().bind(editing);
        okButton.managedProperty().bind(okButton.visibleProperty());
        cancelButton.visibleProperty().bind(editing);
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        editButton.visibleProperty().bind(editing.not());
        editButton.managedProperty().bind(editButton.visibleProperty());

        relationTypeCB.disableProperty().bind(editing.not());
        weightTF.editableProperty().bind(editing);
        descriptionTA.editableProperty().bind(editing);

        Platform.runLater(() -> relationTypeCB.getParent().requestFocus());
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
        relationTypeCB.getSelectionModel().select(edge.getRelationType());
        weightTF.textProperty().set(Double.toString(edge.getWeight()));
        descriptionTA.textProperty().set(edge.getDescription());

        if (edge instanceof BabelNetEdge) {
            buttonBar.setManaged(false);
            buttonBar.setVisible(false);
        }
    }

    public void startEditing() {
        editing.set(true);
    }

    public void cancelEditing() {
        editing.set(false);
        weightTF.textProperty().set(Double.toString(edge.getWeight()));
        descriptionTA.textProperty().set(edge.getDescription());
    }

    public void save() {
        UserEdge originalEdge = (UserEdge) edge;
        UserEdge editedEdge = originalEdge.changeDescription(descriptionTA.getText());
        editedEdge = editedEdge.changeWeight(Double.parseDouble(weightTF.getText()));
        editedEdge = editedEdge.changeRelationType(relationTypeCB.getSelectionModel().getSelectedItem());

        mainController.editEdge(originalEdge, editedEdge);
        editing.set(false);
    }

    public void openRelationTypesListWindow() throws IOException {
        openRelationTypesListWindow((Stage) relationTypeCB.getScene().getWindow());
    }
}
