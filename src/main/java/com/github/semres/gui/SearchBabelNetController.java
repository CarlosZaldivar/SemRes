package com.github.semres.gui;

import com.github.semres.Synset;
import com.guigarage.controls.Media;
import com.guigarage.controls.SimpleMediaListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SearchBabelNetController extends ChildController implements Initializable {
    @FXML
    ListView<SynsetMedia> synsetsListView;

    private ObservableList<SynsetMedia> synsetsObservableList = FXCollections.observableArrayList();


    @FXML
    TextField searchBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        synsetsListView.setCellFactory(v -> new BabelNetSynsetCell<>(this));
        synsetsListView.setItems(synsetsObservableList);
    }

    public void addSynset(MouseEvent click) {
        if (click.getClickCount() == 2 && synsetsListView.getSelectionModel().getSelectedItem() != null) {
            Synset synset = synsetsListView.getSelectionModel().getSelectedItem().getSynset();
            ((MainController) parent).addSynset(synset);
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

        List<Synset> synsetsFound = null;
        try {
            synsetsFound = ((MainController) parent).searchBabelNet(searchPhrase);
        } catch (IOException e) {
            System.out.println("BabelNet connection error.");
        }

        for (Synset synset : synsetsFound) {
            synsetsObservableList.add(new SynsetMedia(synset));
        }
    }
}

class BabelNetSynsetCell<T extends Media> extends SimpleMediaListCell<T> {
    SearchBabelNetController controller;

    BabelNetSynsetCell(SearchBabelNetController controller) {
        this.controller = controller;
        addEventHandler(MouseEvent.MOUSE_CLICKED, controller::addSynset);
    }
}
