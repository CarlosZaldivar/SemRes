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
                        return ((MainController) parent).searchBabelNet(searchBox.getText());
                    }
                };
            }
        };

        searchService.setOnSucceeded(workerStateEvent -> showResults());
        searchService.setOnFailed(workerStateEvent -> {
            if (searchService.getException() instanceof IOException) {
                Utils.showAlert("There was a problem connecting to BabelNet.");
            } else {
                Utils.showAlert(searchService.getException().getMessage());
            }
        });

        downloadEdgesService = new Service<Collection<Edge>>() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Collection<Edge> call() throws Exception {
                        return ((MainController) parent).downloadBabelNetEdges(clickedSynset.getId());
                    }
                };
            }
        };
        downloadEdgesService.setOnSucceeded(workerStateEvent -> addDownloadedEdges());
        downloadEdgesService.setOnFailed(workerStateEvent -> {
            if (searchService.getException() instanceof IOException) {
                Utils.showAlert("There was a problem connecting to BabelNet.");
            } else {
                Utils.showAlert(searchService.getException().getMessage());
            }
        });

        synsetsListView.visibleProperty().bind(searchService.runningProperty().not());
        synsetsListView.disableProperty().bind(downloadEdgesService.runningProperty());
        progressIndicatorVB.visibleProperty().bind(searchService.runningProperty().or(downloadEdgesService.runningProperty()));
    }

    private void showResults() {
        List<BabelNetSynset> synsetsFound = searchService.getValue();

        for (Synset synset : synsetsFound) {
            synsetsObservableList.add(new SynsetMedia(synset));
        }
    }


    private void addDownloadedEdges() {
        ((MainController) parent).addSynsetToView(((MainController) parent).getSynset(clickedSynset.getId()));
        Stage stage = (Stage) synsetsListView.getScene().getWindow();
        stage.close();
    }

    void addSynset(MouseEvent click) {
        if (click.getClickCount() == 2 && synsetsListView.getSelectionModel().getSelectedItem() != null) {
            clickedSynset = (BabelNetSynset) synsetsListView.getSelectionModel().getSelectedItem().getSynset();

            if (((MainController) parent).synsetExists(clickedSynset.getId())) {
                ((MainController) parent).loadSynset(clickedSynset.getId());
                ((MainController) parent).addSynsetToView(((MainController) parent).getSynset(clickedSynset.getId()));
            } else {
                ((MainController) parent).addSynsetToBoard(clickedSynset);
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
