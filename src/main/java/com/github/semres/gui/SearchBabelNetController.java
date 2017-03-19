package com.github.semres.gui;

import com.github.semres.Edge;
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
            try {
                synset.loadEdgesFromBabelNet();
            } catch (IOException | InvalidBabelSynsetIDException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                // Resize dialog so that the whole text would fit.
                alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
                alert.showAndWait();
                return;
            }
            ((MainController) parent).addSynset(synset);
            for (Edge edge : synset.getOutgoingEdges().values()) {
                ((MainController) parent).addEdge(edge);
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
}

class BabelNetSynsetCell<T extends Media> extends SimpleMediaListCell<T> {

    BabelNetSynsetCell(SearchBabelNetController controller) {
        addEventHandler(MouseEvent.MOUSE_CLICKED, controller::addSynset);
    }
}