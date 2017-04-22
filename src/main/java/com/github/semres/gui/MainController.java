package com.github.semres.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.semres.Board;
import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

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
    @FXML
    private MenuItem exportMenuItem;

    private Board board;
    private WebEngine engine;
    private BabelNetManager babelNetManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        babelNetManager = new BabelNetManager();
        engine = boardView.getEngine();
        connectBackendObject(engine, "javaApp", new JavaApp());
    }

    void setBoard(Board board) {
        this.board = board;

        // Reload html
        engine.load(getClass().getResource("/html/board.html").toExternalForm());
        // Enable some options.
        saveMenuItem.setDisable(false);
        exportMenuItem.setDisable(false);
        viewMenu.setDisable(false);
        babelNetMenu.setDisable(false);
    }

    public void save() {
        board.save();
    }

    public void export() {
        // For debuging purposes database triplets are written to clipboard. It can be changed to a file later.
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(board.export());
        clipboard.setContent(content);
    }

    public void openDatabasesWindow() throws IOException {
        openNewWindow("/fxml/databases-list.fxml", "Databases", 300, 275);
    }

    void addSynset(Synset synset) {
        board.addSynset(synset);
        addSynsetToView(synset);
        if (synset.isExpanded()) {
            for (Edge edge : synset.getOutgoingEdges().values()) {
                addEdgeToView(edge);
            }
        }
    }

    void addSynsetToView(Synset synset) {
        engine.executeScript("addSynset(" + synsetToJson(synset) + ");");
    }

    void addEdge(Edge edge) {
        board.addEdge(edge);
        addEdgeToView(edge);
    }

    void addEdgeToView(Edge edge) { engine.executeScript("addEdge(" + edgeToJson(edge) + ");"); }

    Synset getSynset(String id) {
        return board.getSynset(id);
    }

    Collection<Synset> loadSynsets(String searchPhrase) {
        return board.loadSynsets(searchPhrase);
    }

    Synset loadSynset(String id) {
        return board.loadSynset(id);
    }

    Collection<Edge> loadEdges(String synsetId) {
        return board.loadEdges(synsetId);
    }

    boolean synsetExists(String id) {
        return board.isIdAlreadyTaken(id);
    }

    List<? extends Synset> searchBabelNet(String searchPhrase) throws IOException {
        return babelNetManager.getSynsets(searchPhrase);
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
        Map<String, Object> synsetMap;
        try {
            synsetMap = PropertyUtils.describe(synset);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        synsetMap.remove("outgoingEdges");
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
        edgeMap.put("targetSynset", synsetToMap(board.getSynset(edge.getPointedSynset())));
        edgeMap.put("sourceSynset", synsetToMap(board.getSynset(edge.getOriginSynset())));


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
            Synset synset = board.getSynset(synsetId);
            Collection<Edge> edges;
            if (synset.isExpanded()) {
                edges = synset.getOutgoingEdges().values();
            } else {
                edges = MainController.this.loadEdges(synsetId);
            }
            for (Edge edge : edges) {
                MainController.this.addSynsetToView(board.getSynset(edge.getPointedSynset()));
                MainController.this.addEdgeToView(edge);
            }
        }

        public void downloadEdgesFromBabelNet(String synsetId) throws IOException, InvalidBabelSynsetIDException {
            BabelNetSynset synset = (BabelNetSynset) board.getSynset(synsetId);
            Collection<? extends Synset> downloadedSynsets = synset.loadEdgesFromBabelNet();

            // Check if synsets were downloaded earlier and add or load them if necessary.
            for (Synset downloadedSynset : downloadedSynsets) {
                String downloadedSynsetId = downloadedSynset.getId();
                if (!board.isIdAlreadyTaken(downloadedSynsetId)) {
                    MainController.this.addSynset(downloadedSynset);
                } else if (board.getSynset(downloadedSynsetId) == null) {
                    board.loadSynset(downloadedSynsetId);
                }
            }

            for (Edge edge : synset.getOutgoingEdges().values()) {
                MainController.this.addSynsetToView(board.getSynset(edge.getPointedSynset()));
                MainController.this.addEdgeToView(edge);
            }
        }

        public void removeNode(String id) {
            board.removeNode(id);
        }

        public void removeEdge(String id) {
            board.removeEdge(id);
        }
    }
}
