package com.github.semres.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.semres.Board;
import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static es.uvigo.ei.sing.javafx.webview.Java2JavascriptUtils.connectBackendObject;

public class MainController extends Controller implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private WebView boardView;

    @FXML
    private Menu viewMenu;
    @FXML
    private Menu babelNetMenu;
    @FXML
    private MenuItem saveMenuItem;

    public Board getBoard() {
        return board;
    }

    private Board board;
    private WebEngine engine;
    private BabelNetManager babelNetManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        babelNetManager = new BabelNetManager();
        engine = boardView.getEngine();
        connectBackendObject(engine, "javaApp", new JavaApp());
    }

    public void setBoard(Board board) {
        this.board = board;

        // Reload html
        engine.load(getClass().getResource("/html/board.html").toExternalForm());
        // Enable 'save' option.
        saveMenuItem.setDisable(false);
        viewMenu.setDisable(false);
        babelNetMenu.setDisable(false);
    }

    public void save() {
        board.save();
    }

    public void openDatabasesWindow() throws IOException {
        openNewWindow("/fxml/databases-list.fxml", "Databases", 300, 275);
    }

    public void addSynset(Synset synset) {
        board.addSynset(synset);
        addSynsetToView(synset);
    }

    public void addSynsetToView(Synset synset) {
        engine.executeScript("addSynset(" + synsetToJson(synset) + ");");
    }

    public List<Synset> loadSynsets(String searchPhrase) {
        return board.loadSynsets(searchPhrase);
    }

    public List<Synset> searchBabelNet(String searchPhrase) throws IOException {
        return (List) babelNetManager.getSynsets(searchPhrase);
    }

    public List<Synset> searchLoadedSynsets(String searchPhrase) {
        return board.searchLoadedSynsets(searchPhrase);
    }

    public void addEdge(Edge edge) {
        board.addEdge(edge);
    }

    public void openLoadSynsetWindow() throws IOException {
        openNewWindow("/fxml/load-synset.fxml", "Load synset", 500, 350);
    }

    public void openSearchBabelNetWindow() throws IOException {
        openNewWindow("/fxml/search-babelnet.fxml", "Search BabelNet", 500, 350);
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

    private Map<String, Object> synsetToMap(Synset synset) {
        Map<String, Object> synsetMap = new HashMap<>();

        // Temporary measure to avoid errors when trying to send json with colon.
        synsetMap.put("id", synset.getId().replace(':', '_'));
        synsetMap.put("description", synset.getDescription());
        synsetMap.put("representation", synset.getRepresentation());
        return synsetMap;
    }

    private String synsetToJson(Synset synset) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonSynset = null;
        try {
            jsonSynset = mapper.writeValueAsString(synsetToMap(synset));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonSynset;
    }

    private String edgeToJson(Edge edge) {
        Map<String, Object> edgeMap = new HashMap<>();
        edgeMap.put("id", edge.getId());
        edgeMap.put("description", edge.getDescription());
        edgeMap.put("weight", edge.getWeight());
        edgeMap.put("relationType", edge.getRelationType().toString().toLowerCase());
        edgeMap.put("targetSynset", synsetToMap(edge.getPointedSynset()));
        edgeMap.put("sourceSynset", synsetToMap(edge.getOriginSynset()));


        String jsonEdge = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonEdge = mapper.writeValueAsString(edgeMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdge;
    }

    public class JavaApp {
        public void openNewSynsetWindow() throws IOException {
            openNewWindow("/fxml/add-synset.fxml", "Add synset", 500, 350);
        }

        public void openNewEdgeWindow(String originSynsetId, String destinationSynsetId) throws IOException {
            AddingEdgeController childController = (AddingEdgeController) openNewWindow("/fxml/add-edge.fxml", "Edge details", 500, 350);

            childController.setOriginSynset(board.getSynset(originSynsetId));
            childController.setDestinationSynset(board.getSynset(destinationSynsetId));
        }

        public void loadEdges(String synsetId) {
            board.loadEdges(synsetId);
            for (Edge edge : board.getSynset(synsetId).getOutgoingEdges().values()) {
                engine.executeScript("addEdge(" + edgeToJson(edge) + ");");
            }
        }

        public void removeElement(String id) {
            board.removeElement(id);
        }
    }
}
