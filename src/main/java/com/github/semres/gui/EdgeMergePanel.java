package com.github.semres.gui;

import com.github.semres.EdgeEdit;
import com.github.semres.SynsetUpdate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class EdgeMergePanel extends TitledPane {
    @FXML private GridPane gridPane;
    @FXML private Label relationTypesLabel;
    @FXML private Label originalRelationTypeLabel;
    @FXML private Label finalRelationTypeLabel;
    @FXML private Button cancelRelationReplaceButton;
    @FXML private Label descriptionLabel;
    @FXML private Label originalDescriptionLabel;
    @FXML private Label finalDescriptionLabel;
    @FXML private ButtonBar descriptionButtonBar;
    @FXML private Button mergeDescriptionButton;
    @FXML private Label weightLabel;
    @FXML private Label originalWeightLabel;
    @FXML private Label finalWeightLabel;
    @FXML private Button cancelWeightReplaceButton;
    @FXML private Button cancelButton;
    private SynsetUpdate synsetUpdate;
    private EdgeEdit edgeEdit;
    private UpdatesListController parentController;
    public EdgeMergePanel(SynsetUpdate synsetUpdate, EdgeEdit edgeEdit, UpdatesListController parentController) {
        this.synsetUpdate = synsetUpdate;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edge-merge.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.parentController = parentController;
        this.edgeEdit = edgeEdit;
        this.textProperty().setValue(
                synsetUpdate.getOriginalSynset().getRepresentation() + " → " + synsetUpdate.getPointedSynset(edgeEdit.getOriginal()).getRepresentation());

        String original = edgeEdit.getOriginal().getRelationType().getType();
        String edited = edgeEdit.getEdited().getRelationType().getType();

        if (!original.equals(edited)) {
            originalRelationTypeLabel.setText(original);
            finalRelationTypeLabel.setText(edited);
        } else {
            removeRelationTypeRow();
        }

        original = Double.toString(edgeEdit.getOriginal().getWeight());
        edited = Double.toString(edgeEdit.getEdited().getWeight());

        if (!original.equals(edited)) {
            originalWeightLabel.setText(original);
            finalWeightLabel.setText(edited);
        } else {
            removeWeightRow();
        }

        original = nullOrEmpty(edgeEdit.getOriginal().getDescription()) ? "No description" : edgeEdit.getOriginal().getDescription();
        edited = nullOrEmpty(edgeEdit.getEdited().getDescription()) ? "No description" : edgeEdit.getEdited().getDescription();

        if (!original.equals(edited)) {
            originalDescriptionLabel.setText(original);
            finalDescriptionLabel.setText(edited);

            if (original.equals("No description")) {
                mergeDescriptionButton.setVisible(false);
                mergeDescriptionButton.setManaged(false);
            }
        } else {
            removeDescriptionRow();
        }
    }

    public EdgeEdit getEdgeEdit() {
        return edgeEdit;
    }

    public SynsetUpdate getSynsetUpdate() {
        return synsetUpdate;
    }

    public void cancelRelationReplace() {
        synsetUpdate.cancelEdgeRelationTypeChange(edgeEdit.getOriginal().getId());
        removeRelationTypeRow();
        if (noMoreChanges()) {
            removeItself();
        }
    }

    private void removeRelationTypeRow() {
        gridPane.getChildren().remove(relationTypesLabel);
        gridPane.getChildren().remove(originalRelationTypeLabel);
        gridPane.getChildren().remove(finalRelationTypeLabel);
        gridPane.getChildren().remove(cancelRelationReplaceButton);
    }

    public void mergeDescription() {
        synsetUpdate.mergeEdgeDescriptions(edgeEdit.getOriginal().getId());
        descriptionButtonBar.getButtons().remove(mergeDescriptionButton);
    }

    public void cancelDescriptionReplace() {
        synsetUpdate.cancelEdgeDescriptionChange(edgeEdit.getOriginal().getId());
        removeDescriptionRow();
        if (noMoreChanges()) {
            removeItself();
        }
    }

    private void removeDescriptionRow() {
        gridPane.getChildren().remove(descriptionLabel);
        gridPane.getChildren().remove(originalDescriptionLabel);
        gridPane.getChildren().remove(finalDescriptionLabel);
        gridPane.getChildren().remove(descriptionButtonBar);
    }

    public void cancelWeightReplace() {
        synsetUpdate.cancelEdgeWeightChange(edgeEdit.getOriginal().getId());
        removeWeightRow();
        if (noMoreChanges()) {
            removeItself();
        }
    }

    private void removeWeightRow() {
        gridPane.getChildren().remove(weightLabel);
        gridPane.getChildren().remove(originalWeightLabel);
        gridPane.getChildren().remove(finalWeightLabel);
        gridPane.getChildren().remove(cancelWeightReplaceButton);
    }

    private boolean noMoreChanges() {
        return !gridPane.getChildren().contains(relationTypesLabel) && !gridPane.getChildren().contains(descriptionLabel)
                && !gridPane.getChildren().contains(weightLabel);
    }

    public void removeItself() {
        parentController.cancelEdgeMerge(this);
    }

    private boolean nullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
