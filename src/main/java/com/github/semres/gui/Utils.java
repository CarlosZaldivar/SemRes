package com.github.semres.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

final class Utils {
    private Utils() {}

    static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        // Resize dialog so that the whole text would fit.
        alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        alert.showAndWait();
    }
}
