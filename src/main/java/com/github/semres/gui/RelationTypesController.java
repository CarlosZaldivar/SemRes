package com.github.semres.gui;

import com.github.semres.RelationType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collection;

public class RelationTypesController extends ChildController {
    @FXML private ListView<RelationType> listView;
    @FXML private Button addButton;
    @FXML private Button deleteButton;

    private final ObservableList<RelationType> observableList = FXCollections.observableArrayList();

    private AddingEdgeController addingEdgeController;

    @Override
    public void setParent(Controller parent) {
        addingEdgeController = (AddingEdgeController) parent;

        observableList.setAll(addingEdgeController.getRelationTypes());
        listView.setItems(observableList);


        BooleanBinding removalValid = Bindings.createBooleanBinding(() -> {
            RelationType relationType = listView.getSelectionModel().getSelectedItem();
            return relationType != null && !relationType.getSource().equals("BabelNet");
        }, listView.getSelectionModel().selectedItemProperty());
        deleteButton.disableProperty().bind(removalValid.not());
    }

    public void openNewRelationTypeDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-relation-type.fxml"));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setTitle("Add relation type");
        newStage.setScene(new Scene(root));
        newStage.sizeToScene();
        newStage.initOwner(listView.getScene().getWindow());
        newStage.initModality(Modality.WINDOW_MODAL);
        AddingRelationTypeController controller = loader.getController();
        controller.setParent(this);
        newStage.show();
    }

    void addRelationType(RelationType relationType) {
        addingEdgeController.addRelationType(relationType);
        observableList.add(relationType);
    }

    public void removeRelationType() {
        RelationType relationType = listView.getSelectionModel().getSelectedItem();
        if (relationType != null) {
            addingEdgeController.removeRelationType(relationType);
            observableList.remove(relationType);
        }
    }

    public Collection<RelationType> getRelationTypes() {
        return addingEdgeController.getRelationTypes();
    }
}
