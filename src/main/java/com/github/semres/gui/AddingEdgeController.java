package com.github.semres.gui;

import com.github.semres.RelationType;
import com.github.semres.Synset;
import com.github.semres.user.UserEdge;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class AddingEdgeController extends ChildController implements Initializable {
    @FXML private ComboBox<RelationType> relationTypeCB;
    @FXML private TextField weightTF;
    @FXML private TextArea descriptionTA;
    @FXML private Button addButton;

    private Synset originSynset;
    private Synset destinationSynset;
    private ObservableList<RelationType> relationTypes = FXCollections.observableArrayList();

    private MainController mainController;

    public void setOriginSynset(Synset originSynset) {
        this.originSynset = originSynset;
    }

    void setDestinationSynset(Synset destinationSynset) {
        this.destinationSynset = destinationSynset;
    }

    @Override
    public void setParent(Controller parent) {
        mainController = (MainController) parent;
        relationTypes.addAll(mainController.getRelationTypes());
        relationTypeCB.setItems((relationTypes));
        relationTypeCB.getSelectionModel().select(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        addButton.disableProperty().bind(weightValid.not());
    }

    public void addEdge() {
        double weight = Double.parseDouble(weightTF.getText().replace(',', '.'));
        RelationType relationType = relationTypeCB.getSelectionModel().getSelectedItem();
        UserEdge newEdge = new UserEdge(destinationSynset.getId(), originSynset.getId(), descriptionTA.getText(), relationType, weight);

        mainController.addEdge(newEdge);

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
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

    public void openRelationTypesListWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/relation-types-list.fxml"));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setTitle("Relation types list");
        newStage.setScene(new Scene(root, 300, 275));
        newStage.initOwner(relationTypeCB.getScene().getWindow());
        newStage.initModality(Modality.WINDOW_MODAL);
        RelationTypesController controller = loader.getController();
        controller.setParent(this);
        newStage.show();
    }
}
