package com.github.semres.gui;

import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.user.UserEdge;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class AddingEdgeController extends ChildController implements Initializable {
    @FXML
    ComboBox relationTypeCB;
    @FXML
    TextField weightTF;
    @FXML
    Button addButton;

    private Synset originSynset;
    private Synset destinationSynset;

    public void setOriginSynset(Synset originSynset) {
        this.originSynset = originSynset;
    }

    public void setDestinationSynset(Synset destinationSynset) {
        this.destinationSynset = destinationSynset;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        relationTypeCB.getItems().setAll(Edge.RelationType.values());
        relationTypeCB.getSelectionModel().select(0);
        weightTF.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
    }

    public void addEdge() {
        if (weightTF.getTextFormatter().getValue() != null) {
            double weight = Double.parseDouble(weightTF.getTextFormatter().getValue().toString());
            Edge.RelationType relationType = Edge.RelationType.valueOf(relationTypeCB.getSelectionModel().getSelectedItem().toString());
            UserEdge newEdge = new UserEdge(destinationSynset, originSynset, relationType, weight);

            ((MainController) parent).addEdge(newEdge);

            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.close();
        }
    }
}
