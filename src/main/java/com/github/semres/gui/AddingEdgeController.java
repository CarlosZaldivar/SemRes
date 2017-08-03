package com.github.semres.gui;

import com.github.semres.RelationType;
import com.github.semres.Synset;
import com.github.semres.user.UserEdge;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddingEdgeController extends EdgeController implements Initializable {
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
    public void setParent(JavaFXController parent) {
        super.setParent(parent);
        relationTypeCB.setItems((relationTypes));
        relationTypeCB.getSelectionModel().select(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addButton.disableProperty().bind(getWeightInvalidProperty(weightTF));
    }

    public void addEdge() {
        double weight = Double.parseDouble(weightTF.getText().replace(',', '.'));
        RelationType relationType = relationTypeCB.getSelectionModel().getSelectedItem();
        UserEdge newEdge = new UserEdge(destinationSynset.getId(), originSynset.getId(), descriptionTA.getText(), relationType, weight);

        mainController.getBoard().addEdge(newEdge);
        mainController.getBrowserController().addEdge(newEdge);

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }

    public void openRelationTypesListWindow() throws IOException {
        openRelationTypesListWindow((Stage) relationTypeCB.getScene().getWindow());
    }
}
