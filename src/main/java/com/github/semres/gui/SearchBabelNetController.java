package com.github.semres.gui;

import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetSynset;
import com.guigarage.controls.Media;
import com.guigarage.controls.SimpleMediaListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class SearchBabelNetController extends ChildController implements Initializable {
    @FXML private VBox progressIndicatorVB;
    @FXML private ListView<SynsetMedia> synsetsListView;
    @FXML private TextField searchBox;

    private final ObservableList<SynsetMedia> synsetsObservableList = FXCollections.observableArrayList();
    private Service<List<BabelNetSynset>> searchService;
    private Service<Collection<Edge>> downloadEdgesService;

    private BabelNetSynset clickedSynset;
    
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        synsetsListView.setCellFactory(v -> new BabelNetSynsetCell<>(this));
        synsetsListView.setItems(synsetsObservableList);

        searchService = new Service<List<BabelNetSynset>>() {
            @Override
            protected Task<List<BabelNetSynset>> createTask() {
                return new Task<List<BabelNetSynset>>() {
                    @Override
                    protected List<BabelNetSynset> call() throws Exception {
                        return mainController.searchBabelNet(searchBox.getText());
                    }
                };
            }
        };

        searchService.setOnSucceeded(workerStateEvent -> showResults());
        searchService.setOnFailed(workerStateEvent -> {
            if (searchService.getException() instanceof IOException) {
                Utils.showError("There was a problem connecting to BabelNet.");
            } else {
                Utils.showError(searchService.getException().getMessage());
            }
        });

        downloadEdgesService = new Service<Collection<Edge>>() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Collection<Edge> call() throws Exception {
                        return mainController.downloadBabelNetEdges(clickedSynset.getId());
                    }
                };
            }
        };
        downloadEdgesService.setOnSucceeded(workerStateEvent -> addDownloadedEdges());
        downloadEdgesService.setOnFailed(workerStateEvent -> {
            if (searchService.getException() instanceof IOException) {
                Utils.showError("There was a problem connecting to BabelNet.");
            } else {
                Utils.showError(searchService.getException().getMessage());
            }
        });

        synsetsListView.visibleProperty().bind(searchService.runningProperty().not());
        synsetsListView.disableProperty().bind(downloadEdgesService.runningProperty());
        progressIndicatorVB.visibleProperty().bind(searchService.runningProperty().or(downloadEdgesService.runningProperty()));
    }

    @Override
    public void setParent(Controller parent) {
        mainController = (MainController) parent;
    }

    private void showResults() {
        List<BabelNetSynset> synsetsFound = searchService.getValue();

        for (Synset synset : synsetsFound) {
            synsetsObservableList.add(new SynsetMedia(synset));
        }
    }


    private void addDownloadedEdges() {
        mainController.addSynsetToView(mainController.getSynset(clickedSynset.getId()));
        Stage stage = (Stage) synsetsListView.getScene().getWindow();
        stage.close();
    }

    void addSynset(MouseEvent click) {
        if (click.getClickCount() == 2 && synsetsListView.getSelectionModel().getSelectedItem() != null) {
            clickedSynset = (BabelNetSynset) synsetsListView.getSelectionModel().getSelectedItem().getSynset();

            if (mainController.synsetExists(clickedSynset.getId())) {
                mainController.loadSynset(clickedSynset.getId());
                mainController.addSynsetToView(mainController.getSynset(clickedSynset.getId()));
            } else {
                mainController.addSynsetToBoard(clickedSynset);
            }

            downloadEdgesService.restart();
        }
    }

    public void search() {
        synsetsObservableList.clear();
        String searchPhrase = searchBox.getText();
        if (searchPhrase.equals("")) {
            return;
        }

        searchService.restart();
    }
}

class BabelNetSynsetCell<T extends Media> extends SimpleMediaListCell<T> {

    BabelNetSynsetCell(SearchBabelNetController controller) {
        addEventHandler(MouseEvent.MOUSE_CLICKED, controller::addSynset);
    }
}
