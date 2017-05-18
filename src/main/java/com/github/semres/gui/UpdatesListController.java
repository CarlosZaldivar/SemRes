package com.github.semres.gui;

import com.github.semres.Edge;
import com.github.semres.SynsetUpdate;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UpdatesListController extends ChildController implements Initializable {
    @FXML private BorderPane mainPane;
    @FXML private VBox progressIndicatorVB;
    @FXML private Button applyButton;
    @FXML private TableView<EdgeData> removedEdgesTable;
    @FXML private TableView<EdgeData> addedEdgesTable;
    @FXML private TableColumn<EdgeData, String> addedEdgeFromColumn;
    @FXML private TableColumn<EdgeData, String> addedEdgeToColumn;
    @FXML private TableColumn<EdgeData, Edge.RelationType> addedEdgeTypeColumn;
    @FXML private TableColumn<EdgeData, Double> addedEdgeWeightColumn;
    @FXML private TableColumn<EdgeData, String> removedEdgeFromColumn;
    @FXML private TableColumn<EdgeData, String> removedEdgeToColumn;
    @FXML private TableColumn<EdgeData, Edge.RelationType> removedEdgeTypeColumn;
    @FXML private TableColumn<EdgeData, Double> removedEdgeWeightColumn;

    private List<SynsetUpdate> updates;
    private ObservableList<EdgeData> addedEdges;
    private ObservableList<EdgeData> removedEdges;
    private Task<List<SynsetUpdate>> updateTask;

    @Override
    public void setParent(Controller parent) {
        super.setParent(parent);

        // Update task should be run only when MainController is set up.
        Thread updatesThread = new Thread(updateTask);
        updatesThread.setDaemon(true);
        updatesThread.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateTask = new Task<List<SynsetUpdate>>() {
            @Override
            protected List<SynsetUpdate> call() throws IOException, InterruptedException {
                return ((MainController) parent).checkForUpdates();
            }
        };
        mainPane.visibleProperty().bind(updateTask.runningProperty().not());
        mainPane.managedProperty().bind(updateTask.runningProperty().not());
        progressIndicatorVB.visibleProperty().bind(updateTask.runningProperty());
        progressIndicatorVB.managedProperty().bind(updateTask.runningProperty());

        updateTask.setOnSucceeded(workerStateEvent -> showUpdates());
        updateTask.setOnFailed(workerStateEvent -> Utils.showAlert(updateTask.getException().getMessage()));

        addedEdgeFromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        addedEdgeToColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        addedEdgeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        addedEdgeWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        removedEdgeFromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        removedEdgeToColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        removedEdgeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        removedEdgeWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        addedEdges = FXCollections.observableArrayList();
        removedEdges = FXCollections.observableArrayList();

        addedEdgesTable.setItems(addedEdges);
        removedEdgesTable.setItems(removedEdges);
    }

    public void applyUpdates() {
        ((MainController) parent).update(updates);
        Stage stage = (Stage) applyButton.getScene().getWindow();
        stage.close();
    }

    private void showUpdates() {
        updates = updateTask.getValue();
        for (SynsetUpdate update : updates) {
            for (Edge edge : update.getAddedEdges().values()) {
                addEdgeToObservableList(edge, update, addedEdges);
            }

            for (Edge edge : update.getRemovedEdges().values()) {
                addEdgeToObservableList(edge, update, removedEdges);
            }
        }
    }

    private void addEdgeToObservableList(Edge edge, SynsetUpdate update, ObservableList<EdgeData> list) {
        String from = update.getOriginalSynset().getRepresentation();
        String to = update.getPointedSynset(edge).getRepresentation();
        list.add(new EdgeData(from, to, edge.getRelationType().toString(), edge.getWeight()));
    }

    public static class EdgeData {
        private final SimpleStringProperty from;
        private final SimpleStringProperty to;
        private final SimpleStringProperty type;
        private final SimpleDoubleProperty weight;

        public EdgeData(String from, String to, String type, double weight) {
            this.from = new SimpleStringProperty(from);
            this.to = new SimpleStringProperty(to);
            this.type = new SimpleStringProperty(type);
            this.weight = new SimpleDoubleProperty(weight);
        }

        public Double getWeight() {
            return weight.get();
        }

        public String getFrom() {
            return from.get();
        }

        public String getTo() {
            return to.get();
        }

        public String getType() {
            return type.get();
        }
    }
}