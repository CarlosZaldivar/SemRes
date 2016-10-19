package com.github.semres.gui;

import com.github.semres.Board;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends Controller implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private WebView boardView;

    public Board getBoard() {
        return board;
    }

    private Board board;

    public void setBoard(Board board) {
        this.board = board;
    }

    public void openDatabasesWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/databases-list.fxml"));
        Parent root = loader.load();
        Stage databasesStage = new Stage();
        databasesStage.setTitle("Databases");
        databasesStage.setScene(new Scene(root, 300, 275));
        databasesStage.initOwner(menuBar.getScene().getWindow());
        databasesStage.initModality(Modality.WINDOW_MODAL);

        DatabasesController childController = loader.getController();
        childController.setParent(this);

        databasesStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = boardView.getEngine();
        // Add "javaApp" object to javascript window.
        engine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<State>() {
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        if (newState == State.SUCCEEDED) {
                            JSObject window = (JSObject) engine.executeScript("window");
                            window.setMember("javaApp", new JavaApp());
                        }
                    }
                });
        engine.load(getClass().getResource("/html/board.html").toExternalForm());
    }

    public class JavaApp {
        public void openNewSynsetWindow() throws IOException {
            System.out.println("openNewSynsetWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-synset.fxml"));
            Parent root = loader.load();
            Stage addSynsetStage = new Stage();
            addSynsetStage.setTitle("Add synset");
            addSynsetStage.setScene(new Scene(root, 500, 350));
            addSynsetStage.initOwner(menuBar.getScene().getWindow());
            addSynsetStage.initModality(Modality.WINDOW_MODAL);

            AddingSynsetController childController = loader.getController();
            childController.setParent(MainController.this);

            addSynsetStage.show();
        }
    }
}
