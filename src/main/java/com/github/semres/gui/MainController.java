package com.github.semres.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.semres.*;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.user.UserEdge;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController extends Controller implements Initializable {
    static Logger log = Logger.getRootLogger();
    @FXML private MenuBar menuBar;
    @FXML private MenuItem turtleMenuItem;
    @FXML private MenuItem nTriplesMenuItem;
    @FXML private AnchorPane boardPane;
    @FXML private Menu fileMenu;
    @FXML private Menu viewMenu;
    @FXML private Menu babelNetMenu;
    @FXML private Menu exportSubmenu;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem updateMenuItem;
    @FXML private MenuItem searchBabelNetMenuItem;
    private BrowserView boardView;
    private Board board;
    private Browser browser;
    private BabelNetManager babelNetManager;
    private DatabasesManager databasesManager;
    private String newApiKey;

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

        // Enable loading resources from inside jar file.
        BrowserContext browserContext = browser.getContext();
        ProtocolService protocolService = browserContext.getProtocolService();
        protocolService.setProtocolHandler("jar", new JarProtocolHandler());

        // updateMenuItem should be disabled if there are unsaved changes on the board or there's no api key.
        // searchBabelNetMenuItem should be disabled if there's no api key
        babelNetMenu.setOnShowing(e -> {
            searchBabelNetMenuItem.setDisable(board == null || StringUtils.isEmpty(BabelNetManager.getApiKey()));
            updateMenuItem.setDisable(board == null || board.isBoardEdited() || StringUtils.isEmpty(BabelNetManager.getApiKey()));
        });

        fileMenu.setOnShowing(e -> saveMenuItem.setDisable(board == null || !board.isBoardEdited()));
    }

    void setBoard(Board board) {
        this.board = board;
        board.setBabelNetManager(babelNetManager);
        browser.loadURL(getClass().getResource("/html/board.html").toExternalForm());

        // Disable option to update synsets if there's no api key.
        if (StringUtils.isEmpty(BabelNetManager.getApiKey())) {
            browser.addLoadListener(new LoadAdapter() {
                @Override
                public void onFinishLoadingFrame(FinishLoadingEvent event) {
                    if (event.isMainFrame()) {
                        browser.executeJavaScript("disableUpdates()");
                    }
                }
            });
        }

        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
        log.info("Remote debugging URL: " + remoteDebuggingURL);

        // Enable some options.
        saveMenuItem.setDisable(false);
        exportSubmenu.setDisable(false);
        viewMenu.setDisable(false);
    }

    public String getNewApiKey() {
        return newApiKey;
    }

    public void save() {
        board.save();
        browser.executeJavaScriptAndReturnValue("updateStartTime()");
    }

    public void export(ActionEvent event) {
        MenuItem clickedMenuItem = ((MenuItem) event.getSource());

        RDFFormat format;
        FileChooser.ExtensionFilter extensionFilter;
        if (clickedMenuItem == turtleMenuItem) {
            format = RDFFormat.TURTLE;
            extensionFilter = new FileChooser.ExtensionFilter("Turtle text files (*.ttl)", "*.ttl");
        } else if (clickedMenuItem == nTriplesMenuItem) {
            format = RDFFormat.NTRIPLES;
            extensionFilter = new FileChooser.ExtensionFilter("N-Triples text files (*.nt)", "*.nt");
        } else {
            format = RDFFormat.RDFXML;
            extensionFilter = new FileChooser.ExtensionFilter("RDF/XML text files (*.rdf)", "*.rdf");
        }

        String content = board.export(format);

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(extensionFilter);
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try {
                saveFile(content, file);
            } catch (IOException e) {
                Utils.showError("Could not save file.");
            }
        }
    }

    private void saveFile(String content, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
        fileWriter.close();
    }

    void setBabelNetApiKey(String key) throws IOException {
        BabelNetManager.setApiKey(key);
        newApiKey = key;
    }

    void createSynset(String representation, String description) {
        Synset synset = board.createSynset(representation, description);
        addSynsetToView(synset);
    }

    void addSynsetToBoard(BabelNetSynset synset) {
        board.addSynset(synset);
    }

    void addSynsetToView(Synset synset) {
        Collection<Edge> edges = synset.getOutgoingEdges().values();
        List<Synset> pointedSynsets = new ArrayList<>();
        for (Edge edge : edges) {
            pointedSynsets.add(board.getSynset(edge.getPointedSynsetId()));
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

    void loadEdges(String synsetId) {
        board.loadEdges(synsetId);
    }

    Collection<Edge> downloadBabelNetEdges(String synsetId) throws IOException {
        return board.downloadBabelNetEdges(synsetId);
    }

    List<SynsetUpdate> checkForUpdates() throws IOException {
        return board.checkForUpdates();
    }

    public List<SynsetUpdate> checkForUpdates(String checkedSynsetId) throws IOException {
        return board.checkForUpdates(checkedSynsetId);
    }

    public void update(List<SynsetUpdate> updates) {
        board.update(updates);
        redrawNodes();
    }

    private void redrawNodes() {
        JSArray synsetIds =  browser.executeJavaScriptAndReturnValue("clear()").asArray();
        for (int i = 0; i < synsetIds.length(); ++i) {
            String id = synsetIds.get(i).getStringValue();
            Synset synset = board.getSynset(id);
            if (synset != null) {
                addSynsetToView(synset);
            }
        }
    }

    boolean synsetExists(String id) {
        return board.isIdAlreadyTaken(id);
    }

    List<BabelNetSynset> searchBabelNet(String searchPhrase) throws IOException {
        return babelNetManager.searchSynsets(searchPhrase);
    }

    public void openDatabasesWindow() throws IOException {
        openNewWindow("/fxml/databases-list.fxml", "Databases");
    }

    public void openLoadSynsetWindow() throws IOException {
        openNewWindow("/fxml/load-synset.fxml", "Load synset");
    }

    public void openSearchBabelNetWindow() throws IOException {
        openNewWindow("/fxml/search-babelnet.fxml", "Search BabelNet");
    }

    public void openUpdatesWindow() throws IOException {
        openNewWindow("/fxml/updates-list.fxml", "BabelNet updates");
    }

    public void openUpdatesWindow(String checkedSynsetId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/updates-list.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Stage newStage = new Stage();
        newStage.setTitle("BabelNet updates");
        newStage.setScene(new Scene(root));
        newStage.sizeToScene();
        newStage.initOwner(menuBar.getScene().getWindow());
        newStage.initModality(Modality.WINDOW_MODAL);

        UpdatesListController updatesListController = loader.getController();
        updatesListController.setCheckedSynsetId(checkedSynsetId);
        updatesListController.setParent(MainController.this);

        blockBrowserView(newStage);
        newStage.show();
    }

    public void openApiKeyWindow() throws IOException {
        openNewWindow("/fxml/edit-api-key.fxml", "BabelNet API key");
    }

    private Controller openNewWindow(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.setScene(new Scene(root));
        newStage.sizeToScene();
        newStage.initOwner(menuBar.getScene().getWindow());
        newStage.initModality(Modality.WINDOW_MODAL);

        ChildController childController = loader.getController();
        childController.setParent(MainController.this);

        blockBrowserView(newStage);
        newStage.show();
        return childController;
    }

    // Block input on BrowserView and return to default handlers when window is closed.
    private void blockBrowserView(Stage stage) {
        boardView.setMouseEventsHandler((e) -> true);
        boardView.setScrollEventsHandler((e) -> true);
        boardView.setGestureEventsHandler((e) -> true);
        boardView.setKeyEventsHandler((e) -> true);
        stage.setOnHidden((e) -> {
                    boardView.setMouseEventsHandler(null);
                    boardView.setScrollEventsHandler(null);
                    boardView.setGestureEventsHandler(null);
                    boardView.setKeyEventsHandler(null);
                }
        );
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
        ObjectMapper mapper = getMapper();
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
        ObjectMapper mapper = getMapper();
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
        edgeMap.put("targetSynset", synsetToMap(board.getSynset(edge.getPointedSynsetId())));
        edgeMap.put("sourceSynset", synsetToMap(board.getSynset(edge.getOriginSynsetId())));
        edgeMap.put("lastEditedTime", edge.getLastEditedTime());
        edgeMap.put("class", edge.getClass().getCanonicalName());
        return edgeMap;
    }

    private String edgeToJson(Edge edge) {
        String jsonEdge = null;
        ObjectMapper mapper = getMapper();
        try {
            jsonEdge = mapper.writeValueAsString(edgeToMap(edge));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdge;
    }

    private String edgesToJson(Collection<? extends Edge> edges) {
        List<Map<String, Object>> edgeMaps = edges.stream().map(this::edgeToMap).collect(Collectors.toList());
        ObjectMapper mapper = getMapper();
        String jsonEdges = null;
        try {
            jsonEdges = mapper.writeValueAsString(edgeMaps);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdges;
    }

    private ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure mapper to properly parse LocalDateTime
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    public void dispose() {
        browser.dispose();
    }

    public DatabasesManager getDatabasesManager() {
        return databasesManager;
    }

    public void setDatabasesManager(DatabasesManager databasesManager) {
        this.databasesManager = databasesManager;
    }

    public BabelNetManager getBabelNetManager() {
        return babelNetManager;
    }

    public void setBabelNetManager(BabelNetManager babelNetManager) {
        this.babelNetManager = babelNetManager;
    }

    public void exit() {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
    }

    public Collection<RelationType> getRelationTypes() {
        return board.getRelationTypes();
    }

    public void addRelationType(RelationType relationType) {
        board.addRelationType(relationType);
    }

    public void removeRelationType(RelationType relationType) {
        board.removeRelationType(relationType);
    }

    public class JavaApp {
        public void openNewSynsetWindow() {
            Platform.runLater(() -> {
                try {
                    openNewWindow("/fxml/add-synset.fxml","Add synset");
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void openNewEdgeWindow(String originSynsetId, String destinationSynsetId) {
            Platform.runLater(() -> {
                try {
                    AddingEdgeController childController = (AddingEdgeController) openNewWindow("/fxml/add-edge.fxml", "Edge details");
                    childController.setOriginSynset(board.getSynset(originSynsetId));
                    childController.setDestinationSynset(board.getSynset(destinationSynsetId));
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void openSynsetDetailsWindow(String synsetId) {
            Platform.runLater(() -> {
                try {
                    SynsetDetailsController childController =
                            (SynsetDetailsController) openNewWindow("/fxml/synset-details.fxml", "Synset details");
                    childController.setSynset(board.getSynset(synsetId));
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void openEdgeDetailsWindow(String edgeId) {
            Platform.runLater(() -> {
                try {
                    EdgeDetailsController childController =
                            (EdgeDetailsController) openNewWindow("/fxml/edge-details.fxml", "Edge details");
                    childController.setEdge(board.getEdge(edgeId));
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void removeSynset(String id) {
            board.removeSynset(id);
        }

        public void removeEdge(String id) {
            board.removeEdge(id);
        }

        public void loadEdges(String synsetId) {
            Synset synset = board.getSynset(synsetId);
            Collection<Edge> edges;
            if (synset.hasDatabaseEdgesLoaded()) {
                edges = synset.getOutgoingEdges().values();
            } else {
                MainController.this.loadEdges(synsetId);
                edges = board.getSynset(synsetId).getOutgoingEdges().values();
            }
            List<Synset> pointedSynsets = new ArrayList<>();
            for (Edge edge : edges) {
                pointedSynsets.add(board.getSynset(edge.getPointedSynsetId()));
            }

            browser.executeJavaScript(String.format("expandSynset(\"%s\", %s, %s);", synsetId, synsetsToJson(pointedSynsets), edgesToJson(edges)));
        }

        public void downloadEdgesFromBabelNet(String synsetId) {
            Collection<Edge> edges;
            try {
                edges = MainController.this.downloadBabelNetEdges(synsetId);
            } catch (IOException e) {
                Utils.showError(e.getMessage());
                return;
            }
            List<Synset> pointedSynsets = new ArrayList<>();
            for (Edge edge : edges) {
                pointedSynsets.add(board.getSynset(edge.getPointedSynsetId()));
            }

            browser.executeJavaScript(String.format("addBabelNetEdges(\"%s\", %s, %s);", synsetId, synsetsToJson(pointedSynsets), edgesToJson(edges)));
        }

        public void checkForUpdates(String synsetId) {
            Platform.runLater(() -> {
                try {
                    openUpdatesWindow(synsetId);
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }
    }
}
