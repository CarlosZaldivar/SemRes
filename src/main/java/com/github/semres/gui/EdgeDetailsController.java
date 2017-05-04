package com.github.semres.gui;

import com.github.semres.Edge;
import com.github.semres.babelnet.BabelNetEdge;
import com.github.semres.user.UserEdge;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class EdgeDetailsController extends ChildController implements Initializable {
    @FXML private ComboBox<Edge.RelationType> relationTypeCB;
    @FXML private TextField weightTF;
    @FXML private TextArea descriptionTA;
    @FXML private Button editButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    @FXML private ButtonBar buttonBar;
    private BooleanProperty editing = new SimpleBooleanProperty(false);
    private Edge edge;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        relationTypeCB.getItems().setAll(Edge.RelationType.values());

        BooleanBinding weightValid = Bindings.createBooleanBinding(() -> {
            String weightText = weightTF.getText().replace(',', '.');
            double weight;
            try {
                weight = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                return false;
            }
            return weight >=0 && weight <= 1;
        }, weightTF.textProperty());
        saveButton.disableProperty().bind(weightValid.not());

        saveButton.visibleProperty().bind(editing);
        saveButton.managedProperty().bind(saveButton.visibleProperty());
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

        ((MainController) parent).editEdge(originalEdge, editedEdge);
        editing.set(false);
    }
}
