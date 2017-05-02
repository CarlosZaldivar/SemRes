package com.github.semres.gui;

import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetSynset;
import com.guigarage.controls.Media;
import com.guigarage.controls.SimpleMediaListCell;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class SearchBabelNetController extends ChildController implements Initializable {
    @FXML
    private
    ListView<SynsetMedia> synsetsListView;

    private final ObservableList<SynsetMedia> synsetsObservableList = FXCollections.observableArrayList();


    @FXML
    private
    TextField searchBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        synsetsListView.setCellFactory(v -> new BabelNetSynsetCell<>(this));
        synsetsListView.setItems(synsetsObservableList);
    }

    void addSynset(MouseEvent click) {
        if (click.getClickCount() == 2 && synsetsListView.getSelectionModel().getSelectedItem() != null) {
            BabelNetSynset synset = (BabelNetSynset) synsetsListView.getSelectionModel().getSelectedItem().getSynset();

            // If synset is already in the database or on the board, load it from there.
            if (((MainController) parent).synsetExists(synset.getId())) {
                handleExistingSynset(synset.getId());
            } else {
                handleNewSynset(synset);
            }

            Stage stage = (Stage) synsetsListView.getScene().getWindow();
            stage.close();
        }
    }

    public void search() {
        synsetsObservableList.clear();
        String searchPhrase = searchBox.getText();
        if (searchPhrase.equals("")) {
            return;
        }

        List<? extends Synset> synsetsFound;
        try {
            synsetsFound = ((MainController) parent).searchBabelNet(searchPhrase);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "There was a problem connecting to BabelNet.");
            alert.showAndWait();
            return;
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            // Resize dialog so that the whole text would fit.
            alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
            alert.showAndWait();
            return;
        }

        for (Synset synset : synsetsFound) {
            synsetsObservableList.add(new SynsetMedia(synset));
        }
    }

    private void handleExistingSynset(String synsetId) {
        BabelNetSynset synset = (BabelNetSynset) ((MainController) parent).loadSynset(synsetId);

        if (!synset.isExpanded()) {
            ((MainController) parent).loadEdges(synset.getId());
        }
        if (!synset.isDownloadedWithEdges()) {
            Collection<BabelNetSynset> relatedSynsets;
            try {
                relatedSynsets = synset.loadEdgesFromBabelNet();
            } catch (IOException | InvalidBabelSynsetIDException e) {
                showAlert(e.getMessage());
                return;
            }

            // Add or load connected synsets first before adding the "central" synset. Otherwise there can be exceptions
            // thrown due to missing edge endings.
            for (Synset relatedSynset : relatedSynsets) {
                try {
                    ((MainController) parent).addSynsetToBoard(relatedSynset);
                } catch (IDAlreadyTakenException e) {
                    ((MainController) parent).loadSynset(relatedSynset.getId());
                }
            }
        }

        ((MainController) parent).addSynsetToView(synset);
    }

    private void handleNewSynset(BabelNetSynset synset) {
        List<? extends Synset> relatedSynsets;
        try {
            relatedSynsets = synset.loadEdgesFromBabelNet();
        } catch (IOException | InvalidBabelSynsetIDException | RuntimeException e) {
            showAlert(e.getMessage());
            return;
        }

        // Add or load connected synsets first before adding the "central" synset. Otherwise there can be exceptions
        // thrown due to missing edge endings.
        for (Synset relatedSynset : relatedSynsets) {
            try {
                ((MainController) parent).addSynsetToBoard(relatedSynset);
            } catch (IDAlreadyTakenException e) {
                ((MainController) parent).loadSynset(relatedSynset.getId());
            }
        }
        ((MainController) parent).addSynset(synset);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        // Resize dialog so that the whole text would fit.
        alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        alert.showAndWait();
    }
}

class BabelNetSynsetCell<T extends Media> extends SimpleMediaListCell<T> {

    BabelNetSynsetCell(SearchBabelNetController controller) {
        addEventHandler(MouseEvent.MOUSE_CLICKED, controller::addSynset);
    }
}
