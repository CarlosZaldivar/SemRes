package com.github.semres.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.URL;
import java.util.ResourceBundle;

public class AddingDatabaseController extends ChildController implements Initializable {
    @FXML private TextField newDatabaseNameTF;
    @FXML private TextField newDatabaseBaseIriTF;
    @FXML private Button createButton;

    private DatabasesController databasesController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);

        BooleanBinding urlInvalid = Bindings.createBooleanBinding(() -> !urlValidator.isValid(newDatabaseBaseIriTF.getText()),
            newDatabaseBaseIriTF.textProperty());
        createButton.disableProperty().bind(newDatabaseNameTF.textProperty().isEmpty().or(urlInvalid));

        newDatabaseBaseIriTF.setText("http://example.org");
    }

    @Override
    public void setParent(Controller parent) {
        databasesController = (DatabasesController) parent;
    }

    public void createNewDatabase() {
        try {
            String newDatabaseName = newDatabaseNameTF.getText();
            String newDatabaseBaseIri = newDatabaseBaseIriTF.getText();

            newDatabaseBaseIri = newDatabaseBaseIri.endsWith("/") ? newDatabaseBaseIri : newDatabaseBaseIri + "/";

            databasesController.addDatabase(newDatabaseName, newDatabaseBaseIri);
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.close();
        } catch (IllegalArgumentException e) {
            Utils.showError("Database name already taken.");
        }
    }
}
