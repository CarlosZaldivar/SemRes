package com.github.semres.gui;

import com.github.semres.SemRes;
import com.github.semres.Synset;
import com.guigarage.controls.Media;
import com.guigarage.controls.SimpleMediaListCell;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class LoadSynsetController extends ChildController implements Initializable {
    @FXML private ListView<SynsetMedia> synsetsListView;
    @FXML private TextField searchBox;

    private final ObservableList<SynsetMedia> synsetsObservableList = FXCollections.observableArrayList();

    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        synsetsListView.setCellFactory(v -> new LoadedSynsetCell<>(this));
        synsetsListView.setItems(synsetsObservableList);
    }

    @Override
    public void setParent(Controller parent) {
        mainController = (MainController) parent;
    }

    void addSynsetToView(MouseEvent click) {
        if (click.getClickCount() == 2 && synsetsListView.getSelectionModel().getSelectedItem() != null) {
            Synset synset = synsetsListView.getSelectionModel().getSelectedItem().getSynset();
            mainController.getBrowserController().addSynsetToView(synset);
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

        Collection<Synset> synsetsFound = mainController.getBoard().loadSynsets(searchPhrase);

        for (Synset synset : synsetsFound) {
            synsetsObservableList.add(new SynsetMedia(synset));
        }
    }
}

class LoadedSynsetCell<T extends Media> extends SimpleMediaListCell<T> {

    LoadedSynsetCell(LoadSynsetController controller) {
        addEventHandler(MouseEvent.MOUSE_CLICKED, controller::addSynsetToView);
    }
}

class SynsetMedia implements Media {

    private Synset synset;

    SynsetMedia(Synset synset) {
        this.synset = synset;
    }

    @Override
    public StringProperty titleProperty() {
        return new SimpleStringProperty(getName());
    }

    @Override
    public StringProperty descriptionProperty() {
        return new SimpleStringProperty(getDescription());
    }

    @Override
    public ObjectProperty<Image> imageProperty() {
        File file = new File(getThumbnailPath());
        return new SimpleObjectProperty<>(new Image(file.toURI().toString()));
    }

    private String getDescription() {
        return synset.getDescription();
    }

    private String getName() {
        return synset.getRepresentation();
    }

    private String getThumbnailPath() {

        return SemRes.getBaseDirectory() + "img/no_image.jpg";
    }

    public Synset getSynset() { return synset; }

    public void setSynset(Synset synset) {
        this.synset = synset;
    }
}
