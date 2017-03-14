package com.github.semres.gui;

import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.user.UserEdge;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddingEdgeController extends ChildController implements Initializable {
    @FXML
    private
    ComboBox<Edge.RelationType> relationTypeCB;
    @FXML
    private
    TextField weightTF;
    @FXML
    private
    Button addButton;

    private Synset originSynset;
    private Synset destinationSynset;

    public void setOriginSynset(Synset originSynset) {
        this.originSynset = originSynset;
    }

    void setDestinationSynset(Synset destinationSynset) {
        this.destinationSynset = destinationSynset;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        relationTypeCB.getItems().setAll(Edge.RelationType.values());
        relationTypeCB.getSelectionModel().select(0);

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
        Edge.RelationType relationType = Edge.RelationType.valueOf(relationTypeCB.getSelectionModel().getSelectedItem().toString());
        UserEdge newEdge = new UserEdge(destinationSynset, originSynset, relationType, weight);

        ((MainController) parent).addEdge(newEdge);

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }
}
