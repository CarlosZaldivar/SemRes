package com.github.semres.gui;

import com.github.semres.RelationType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collection;

public class EdgeController extends ChildController {
    protected MainController mainController;
    protected ObservableList<RelationType> relationTypes = FXCollections.observableArrayList();

    @Override
    public void setParent(Controller parent) {
        mainController = (MainController) parent;
        relationTypes.addAll(mainController.getRelationTypes());
    }

    public Collection<RelationType> getRelationTypes() {
        return relationTypes;
    }

    public void addRelationType(RelationType relationType) {
        mainController.addRelationType(relationType);
        relationTypes.add(relationType);
    }

    public void removeRelationType(RelationType relationType) {
        mainController.removeRelationType(relationType);
        relationTypes.remove(relationType);
    }

    void openRelationTypesListWindow(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/relation-types-list.fxml"));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setTitle("Relation types list");
        newStage.setScene(new Scene(root));
        newStage.sizeToScene();
        newStage.initOwner(stage);
        newStage.initModality(Modality.WINDOW_MODAL);
        RelationTypesController controller = loader.getController();
        controller.setParent(this);
        newStage.show();
    }

    BooleanBinding getWeightInvalidProperty(TextField weightTF) {
        return Bindings.createBooleanBinding(() -> {
            String weightText = weightTF.getText().replace(',', '.');
            double weight;
            try {
                weight = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                return true;
            }
            return weight < 0 || weight > 1;
        }, weightTF.textProperty());
    }
}
