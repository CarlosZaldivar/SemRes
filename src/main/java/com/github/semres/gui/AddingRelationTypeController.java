package com.github.semres.gui;

import com.github.semres.RelationType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Collection;

public class AddingRelationTypeController extends ChildController {
    @FXML private TextField nameTF;
    @FXML private Button addButton;

    private RelationTypesController relationTypesController;

    public void addNewRelationType() {
        relationTypesController.addRelationType(new RelationType(nameTF.getText(), "User"));
        Stage stage = (Stage) nameTF.getScene().getWindow();
        stage.close();
    }

    @Override
    public void setParent(Controller parent) {
        relationTypesController = (RelationTypesController) parent;
        Collection<RelationType> currentRelationTypes = ((RelationTypesController) parent).getRelationTypes();
        BooleanBinding nameValid = Bindings.createBooleanBinding(() -> {
            String nameText = nameTF.getText();
            return !nameText.isEmpty() && currentRelationTypes.stream().noneMatch(r -> r.getType().equalsIgnoreCase(nameText));
        }, nameTF.textProperty());
        addButton.disableProperty().bind(nameValid.not());
    }
}
