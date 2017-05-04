package com.github.semres.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.semres.Board;
import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.user.UserEdge;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import javafx.application.Platform;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController extends Controller implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private AnchorPane boardPane;

    @FXML
    private Menu viewMenu;
    @FXML
    private Menu babelNetMenu;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem exportMenuItem;

    private Board board;
    private BrowserView boardView;
    private Browser browser;
    private BabelNetManager babelNetManager;
    static Logger log = Logger.getRootLogger();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add developer tools
        BrowserPreferences.setChromiumSwitches("--remote-debugging-port=9222");
        babelNetManager = new BabelNetManager();
        browser = new Browser();
        boardView = new BrowserView(browser);

        AnchorPane.setTopAnchor(boardView, 0.0);
        AnchorPane.setBottomAnchor(boardView, 0.0);
        AnchorPane.setLeftAnchor(boardView, 0.0);
        AnchorPane.setRightAnchor(boardView, 0.0);

        boardPane.getChildren().add(boardView);

        // Add javaApp object to javascript.
        browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                Browser browser = event.getBrowser();
                JSValue window = browser.executeJavaScriptAndReturnValue("window");
                window.asObject().setProperty("javaApp", new JavaApp());
            }
        });
    }

    void setBoard(Board board) {
        this.board = board;
        browser.loadURL(getClass().getResource("/html/board.html").toExternalForm());
        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
        log.info("Remote debugging URL: " + remoteDebuggingURL);

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
        addSynsetToBoard(synset);
        addSynsetToView(synset);
    }

    void addSynsetToBoard(Synset synset) {
        board.addSynset(synset);
    }

    void addSynsetToView(Synset synset) {
        Collection<Edge> edges = synset.getOutgoingEdges().values();
        List<Synset> pointedSynsets = new ArrayList<>();
        for (Edge edge : edges) {
            pointedSynsets.add(board.getSynset(edge.getPointedSynset()));
        }
        browser.executeJavaScript(String.format("addSynset(%s, %s, %s)", synsetToJson(synset), synsetsToJson(pointedSynsets), edgesToJson(edges)));
    }

    void addEdge(Edge edge) {
        board.addEdge(edge);
        addEdgeToView(edge);
    }

    void addEdgeToView(Edge edge) { browser.executeJavaScript("addEdge(" + edgeToJson(edge) + ");"); }

    void editSynset(Synset oldSynset, Synset editedSynset) {
        board.editSynset(oldSynset, editedSynset);
        browser.executeJavaScript("updateSynset(" + synsetToJson(editedSynset) + ");");
    }

    public void editEdge(UserEdge oldEdge, UserEdge editedEdge) {
        board.editEdge(oldEdge, editedEdge);
        browser.executeJavaScript("updateEdge(" + edgeToJson(editedEdge) + ");");
    }

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

    private String synsetsToJson(Collection<? extends Synset> synsets) {
        List<Map<String, Object>> synsetMaps = synsets.stream().map(this::synsetToMap).collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();
        String jsonSynsets = null;
        try {
            jsonSynsets = mapper.writeValueAsString(synsetMaps);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonSynsets;
    }

    private Map<String, Object> edgeToMap(Edge edge) {
        Map<String, Object> edgeMap = new HashMap<>();
        edgeMap.put("id", edge.getId());
        edgeMap.put("description", edge.getDescription());
        edgeMap.put("weight", edge.getWeight());
        edgeMap.put("relationType", edge.getRelationType().toString().toLowerCase());
        edgeMap.put("targetSynset", synsetToMap(board.getSynset(edge.getPointedSynset())));
        edgeMap.put("sourceSynset", synsetToMap(board.getSynset(edge.getOriginSynset())));
        return edgeMap;
    }

    private String edgeToJson(Edge edge) {
        String jsonEdge = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonEdge = mapper.writeValueAsString(edgeToMap(edge));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdge;
    }

    private String edgesToJson(Collection<? extends Edge> edges) {
        List<Map<String, Object>> edgeMaps = edges.stream().map(this::edgeToMap).collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();
        String jsonEdges = null;
        try {
            jsonEdges = mapper.writeValueAsString(edgeMaps);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdges;
    }

    public void close() {
        browser.dispose();
    }

    public class JavaApp {
        public void openNewSynsetWindow() {
            Platform.runLater(() -> {
                try {
                    openNewWindow("/fxml/add-synset.fxml","Add synset",500,350);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void openNewEdgeWindow(String originSynsetId, String destinationSynsetId) {
            Platform.runLater(() -> {
                try {
                    AddingEdgeController childController = (AddingEdgeController) openNewWindow("/fxml/add-edge.fxml", "Edge details", 500, 350);
                    childController.setOriginSynset(board.getSynset(originSynsetId));
                    childController.setDestinationSynset(board.getSynset(destinationSynsetId));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void openSynsetDetailsWindow(String synsetId) {
            Platform.runLater(() -> {
                try {
                    SynsetDetailsController childController =
                            (SynsetDetailsController) openNewWindow("/fxml/synset-details.fxml", "Synset details", 500, 350);
                    childController.setSynset(board.getSynset(synsetId));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void openEdgeDetailsWindow(String edgeId) {
            Platform.runLater(() -> {
                try {
                    EdgeDetailsController childController =
                            (EdgeDetailsController) openNewWindow("/fxml/edge-details.fxml", "Edge details", 500, 350);
                    childController.setEdge(board.getEdge(edgeId));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void loadEdges(String synsetId) {
            Synset synset = board.getSynset(synsetId);
            Collection<Edge> edges;
            if (synset.isExpanded()) {
                edges = synset.getOutgoingEdges().values();
            } else {
                edges = MainController.this.loadEdges(synsetId);
            }
            List<Synset> pointedSynsets = new ArrayList<>();
            for (Edge edge : edges) {
                pointedSynsets.add(board.getSynset(edge.getPointedSynset()));
            }

            browser.executeJavaScript(String.format("expandSynset(\"%s\", %s, %s);", synsetId, synsetsToJson(pointedSynsets), edgesToJson(edges)));
        }

        public void downloadEdgesFromBabelNet(String synsetId) throws IOException, InvalidBabelSynsetIDException {
            BabelNetSynset synset = (BabelNetSynset) board.getSynset(synsetId);
            Collection<? extends Synset> downloadedSynsets = synset.loadEdgesFromBabelNet();

            // Check if synsets were downloaded earlier and add or load them if necessary.
            for (Synset downloadedSynset : downloadedSynsets) {
                String downloadedSynsetId = downloadedSynset.getId();
                if (!board.isIdAlreadyTaken(downloadedSynsetId)) {
                    MainController.this.addSynsetToBoard(downloadedSynset);
                } else if (board.getSynset(downloadedSynsetId) == null) {
                    board.loadSynset(downloadedSynsetId);
                }
            }

            Collection<Edge> edges = synset.getOutgoingEdges().values();
            List<Synset> pointedSynsets = new ArrayList<>();
            for (Edge edge : synset.getOutgoingEdges().values()) {
                pointedSynsets.add(board.getSynset(edge.getPointedSynset()));
            }

            browser.executeJavaScript(String.format("addBabelNetEdges(\"%s\", %s, %s);", synsetId, synsetsToJson(pointedSynsets), edgesToJson(edges)));
        }
    }
}
