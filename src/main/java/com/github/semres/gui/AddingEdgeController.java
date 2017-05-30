package com.github.semres.gui;

import com.github.semres.RelationType;
import com.github.semres.Synset;
import com.github.semres.user.UserEdge;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddingEdgeController extends ChildController implements Initializable {
    @FXML private ComboBox<RelationType> relationTypeCB;
    @FXML private TextField weightTF;
    @FXML private TextArea descriptionTA;
    @FXML private Button addButton;

    private Synset originSynset;
    private Synset destinationSynset;

    public void setOriginSynset(Synset originSynset) {
        this.originSynset = originSynset;
    }

    void setDestinationSynset(Synset destinationSynset) {
        this.destinationSynset = destinationSynset;
    }

    @Override
    public void setParent(Controller parent) {
        super.setParent(parent);
        relationTypeCB.getItems().setAll(((MainController) parent).getRelationTypes());
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

        ((MainController) parent).addEdge(newEdge);

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }
}
