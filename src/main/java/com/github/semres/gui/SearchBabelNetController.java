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
                        return mainController.getBabelNetManager().searchSynsets(searchBox.getText());
                    }
                };
            }
        };

        searchService.setOnSucceeded(workerStateEvent -> showResults());
        setServiceErrorHandling(searchService);

        downloadEdgesService = new Service<Collection<Edge>>() {
            @Override
            protected Task<Collection<Edge>> createTask() {
                return new Task<Collection<Edge>>() {
                    @Override
                    protected Collection<Edge> call() throws Exception {
                        return mainController.getBoard().downloadBabelNetEdges(clickedSynset.getId());
                    }
                };
            }
        };
        downloadEdgesService.setOnSucceeded(workerStateEvent -> addSynsetToView());
        setServiceErrorHandling(downloadEdgesService);

        synsetsListView.visibleProperty().bind(searchService.runningProperty().not());
        synsetsListView.disableProperty().bind(downloadEdgesService.runningProperty());
        progressIndicatorVB.visibleProperty().bind(searchService.runningProperty().or(downloadEdgesService.runningProperty()));
    }

    private void setServiceErrorHandling(Service service) {
        service.setOnFailed(workerStateEvent -> {
            if (service.getException() instanceof IOException) {
                Utils.showError("There was a problem connecting to BabelNet.");
            } else if (service.getException() != null) {
                Utils.showError(service.getException().getMessage());
            } else {
                Utils.showError("Error occurred");
            }
        });
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


    private void addSynsetToView() {
        mainController.getBrowserController().addSynsetToView(mainController.getBoard().getSynset(clickedSynset.getId()));
        Stage stage = (Stage) synsetsListView.getScene().getWindow();
        stage.close();
    }

    void addSynset(MouseEvent click) {
        if (click.getClickCount() == 2 && synsetsListView.getSelectionModel().getSelectedItem() != null) {
            clickedSynset = (BabelNetSynset) synsetsListView.getSelectionModel().getSelectedItem().getSynset();

            if (mainController.getBoard().synsetExists(clickedSynset.getId())) {
                BabelNetSynset loaded = (BabelNetSynset) mainController.getBoard().loadSynset(clickedSynset.getId());
                if (!loaded.hasDatabaseEdgesLoaded()) {
                    mainController.getBoard().loadEdges(clickedSynset.getId());
                }
                if (!loaded.isDownloadedWithEdges()) {
                    downloadEdgesService.restart();
                } else {
                    addSynsetToView();
                }
            } else {
                mainController.getBoard().addSynset(clickedSynset);
                downloadEdgesService.restart();
            }
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
