package com.github.semres.gui;

import com.github.semres.*;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynset;
import com.github.semres.user.UserEdge;
import com.github.semres.user.UserSynset;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
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
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController extends Controller implements Initializable {
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
    private Board board;
    private BabelNetManager babelNetManager;
    private DatabasesManager databasesManager;
    private String newApiKey;

    private BrowserController browserController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add developer tools
        BrowserPreferences.setChromiumSwitches("--remote-debugging-port=9222");
        babelNetManager = new BabelNetManager();

        browserController = new BrowserController(this);
        BrowserView boardView = browserController.getBoardView();

        AnchorPane.setTopAnchor(boardView, 0.0);
        AnchorPane.setBottomAnchor(boardView, 0.0);
        AnchorPane.setLeftAnchor(boardView, 0.0);
        AnchorPane.setRightAnchor(boardView, 0.0);

        boardPane.getChildren().add(boardView);

        // updateMenuItem should be disabled if there are unsaved changes on the board or there's no api key.
        // searchBabelNetMenuItem should be disabled if there's no api key
        babelNetMenu.setOnShowing(e -> {
            searchBabelNetMenuItem.setDisable(board == null || StringUtils.isEmpty(BabelNetManager.getApiKey()));
            updateMenuItem.setDisable(board == null || board.isEdited() || StringUtils.isEmpty(BabelNetManager.getApiKey()));
        });

        fileMenu.setOnShowing(e -> saveMenuItem.setDisable(board == null || !board.isEdited()));
    }

    void setBoard(Board board) {
        this.board = board;
        board.setBabelNetManager(babelNetManager);

        browserController.loadPage();

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
        browserController.updateStartTime();
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
        browserController.addSynsetToView(synset);
    }

    void addSynsetToBoard(BabelNetSynset synset) {
        board.addSynset(synset);
    }

    void addEdge(Edge edge) {
        board.addEdge(edge);
        browserController.addEdgeToView(edge);
    }

    void editSynset(UserSynset oldSynset, UserSynset editedSynset) {
        board.editSynset(oldSynset, editedSynset);
        browserController.updateSynset(editedSynset);
    }

    public void editEdge(UserEdge oldEdge, UserEdge editedEdge) {
        board.editEdge(oldEdge, editedEdge);
        browserController.updateEdge(editedEdge);
    }

    public void removeSynset(String synsetId) {
        board.removeSynset(synsetId);
    }

    public void removeEdge(String edgeId) {
        board.removeEdge(edgeId);
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

    Collection<Edge> downloadBabelNetEdges(String synsetId) throws IOException {
        return board.downloadBabelNetEdges(synsetId);
    }

    List<SynsetUpdate> checkForUpdates() throws IOException {
        return board.checkForUpdates();
    }

    public SynsetUpdate checkForUpdates(String checkedSynsetId) throws IOException {
        return board.checkForUpdates(checkedSynsetId);
    }

    public void update(List<SynsetUpdate> updates) {
        board.update(updates);
        browserController.redrawNodes();
    }

    boolean synsetExists(String id) {
        return board.synsetExists(id);
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

        browserController.blockBrowserView(newStage);
        newStage.show();
    }

    public void openApiKeyWindow() throws IOException {
        openNewWindow("/fxml/edit-api-key.fxml", "BabelNet API key");
    }

    public void openNewSynsetWindow() throws IOException {
        openNewWindow("/fxml/add-synset.fxml","Add synset");
    }

    public void openNewEdgeWindow(String originSynsetId, String destinationSynsetId) throws IOException {
        AddingEdgeController childController = (AddingEdgeController) openNewWindow("/fxml/add-edge.fxml", "Edge details");
        childController.setOriginSynset(board.getSynset(originSynsetId));
        childController.setDestinationSynset(board.getSynset(destinationSynsetId));
    }

    public void openSynsetDetailsWindow(String synsetId) throws IOException {
        SynsetDetailsController childController =
                (SynsetDetailsController) openNewWindow("/fxml/synset-details.fxml", "Synset details");
        childController.setSynset(board.getSynset(synsetId));
    }

    public void openEdgeDetailsWindow(String edgeId) throws IOException {
        EdgeDetailsController childController =
                (EdgeDetailsController) openNewWindow("/fxml/edge-details.fxml", "Edge details");
        childController.setEdge(board.getEdge(edgeId));
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

        browserController.blockBrowserView(newStage);
        newStage.show();
        return childController;
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

    public BrowserController getBrowserController() {
        return browserController;
    }

    public void dispose() {
        browserController.dispose();
    }

    public void exit() {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
    }
}
