package com.github.semres.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.semres.Board;
import com.github.semres.Edge;
import com.github.semres.Synset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.nashorn.api.scripting.JSObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static es.uvigo.ei.sing.javafx.webview.Java2JavascriptUtils.connectBackendObject;

public class MainController extends Controller implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private WebView boardView;

    @FXML
    private MenuItem saveMenuItem;

    public Board getBoard() {
        return board;
    }

    private Board board;
    private WebEngine engine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        engine = boardView.getEngine();

        connectBackendObject(engine, "javaApp", new JavaApp());
    }

    public void setBoard(Board board) {
        this.board = board;

        engine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            for (Synset synset : board.getSynsets().values()) {
                                ObjectMapper mapper = new ObjectMapper();
                                String jsonSynset = null;
                                try {
                                    jsonSynset = mapper.writeValueAsString(synset);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                                engine.executeScript("addSynset(" + jsonSynset + ");");
                            }
                        }
                    }
                }
        );
        // Reload html
        engine.load(getClass().getResource("/html/board.html").toExternalForm());
        // Enable 'save' option.
        saveMenuItem.setDisable(false);
    }

    public void save() {
        board.save();
    }

    public void openDatabasesWindow() throws IOException {
        openNewWindow("/fxml/databases-list.fxml", "Databases", 300, 275);
    }

    public void addSynset(Synset synset) {
        board.addSynset(synset);
        ObjectMapper mapper = new ObjectMapper();
        String jsonSynset = null;
        try {
            jsonSynset = mapper.writeValueAsString(synset);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        engine.executeScript("addSynset(" + jsonSynset + ");");
    }

    public void addEdge(Edge edge) {
        board.addEdge(edge);
    }

    public class JavaApp {
        public void openNewSynsetWindow() throws IOException {
            openNewWindow("/fxml/add-synset.fxml", "Add synset", 500, 350);
        }

        public void openNewEdgeWindow(String originSynsetId, String destinationSynsetId) throws IOException {
            AddingEdgeController childController = (AddingEdgeController) openNewWindow("/fxml/add-edge.fxml", "Edge details", 500, 350);

            System.out.println(originSynsetId);
            System.out.println(destinationSynsetId);

            childController.setOriginSynset(board.getSynset(originSynsetId));
            childController.setDestinationSynset(board.getSynset(destinationSynsetId));
        }
    }

    private Controller openNewWindow(String fxmlPath, String title, int width, int height) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.setScene(new Scene(root, width, height));
        newStage.initOwner(menuBar.getScene().getWindow());
        newStage.initModality(Modality.WINDOW_MODAL);

        ChildController childController = loader.getController();
        childController.setParent(MainController.this);
        newStage.show();
        return childController;
    }
}
