package com.github.semres.gui;

import com.github.semres.EdgeEdit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class EdgeMergePanel extends BorderPane {
    @FXML private Label edgeNameLabel;

    private EdgeEdit edgeEdit;
    private UpdatesListController parentController;

    public EdgeMergePanel(EdgeEdit edgeEdit, UpdatesListController parentController) {
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
        edgeNameLabel.setText(edgeEdit.getOriginSynset() + "-" + edgeEdit.getPointedSynset());
    }
}
