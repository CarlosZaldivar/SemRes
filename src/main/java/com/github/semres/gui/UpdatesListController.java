package com.github.semres.gui;

import com.github.semres.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class UpdatesListController extends ChildController implements Initializable {
    @FXML private Accordion accordion;
    @FXML private TitledPane editedEdgesTP;
    @FXML private TitledPane editedSynsetsTP;
    @FXML private TitledPane duplicateEdgesTP;
    @FXML private TitledPane removedSynsetsTP;
    @FXML private TitledPane addedEdgesTP;
    @FXML private TitledPane removedEdgesTP;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane stackPane;
    @FXML private BorderPane mainPane;
    @FXML private VBox edgeMergesVB;
    @FXML private VBox progressIndicatorVB;
    @FXML private Button applyButton;
    @FXML private TableView<EdgeData> removedEdgesTable;
    @FXML private TableView<EdgeData> addedEdgesTable;
    @FXML private TableView<Synset> removedSynsetsTable;
    @FXML private TableColumn<EdgeData, String> addedEdgeFromColumn;
    @FXML private TableColumn<EdgeData, String> addedEdgeToColumn;
    @FXML private TableColumn<EdgeData, RelationType> addedEdgeTypeColumn;
    @FXML private TableColumn<EdgeData, Double> addedEdgeWeightColumn;
    @FXML private TableColumn<EdgeData, String> addedEdgeCancelColumn;
    @FXML private TableColumn<EdgeData, String> removedEdgeFromColumn;
    @FXML private TableColumn<EdgeData, String> removedEdgeToColumn;
    @FXML private TableColumn<EdgeData, RelationType> removedEdgeTypeColumn;
    @FXML private TableColumn<EdgeData, Double> removedEdgeWeightColumn;
    @FXML private TableColumn<EdgeData, String> removedEdgeCancelColumn;
    @FXML private TableColumn<Synset, String> removedSynsetIdColumn;
    @FXML private TableColumn<Synset, String> removedSynsetRepresentationColumn;
    @FXML private TableColumn<Synset, String> removedSynsetDescriptionColumn;
    @FXML private TableColumn<Synset, String> removedSynsetCancelColumn;
    @FXML private ListView<Synset> editedSynsetsLV;
    @FXML private ListView<Edge> editedEdgesLV;

    private List<SynsetUpdate> updates;
    private ObservableList<EdgeData> addedEdges;
    private ObservableList<EdgeData> removedEdges;
    private ObservableList<Synset> removedSynsets;
    private ObservableList<Synset> editedSynsets;
    private ObservableList<Edge> editedEdges;
    private Task<List<SynsetUpdate>> updateTask;

    private MainController mainController;
    private boolean checkSingleSynset;
    private String checkedSynsetId;

    @Override
    public void setParent(JavaFXController parent) {
        mainController = (MainController) parent;

        // Update task should be run only when MainController is set up.
        Thread updatesThread = new Thread(updateTask);
        updatesThread.setDaemon(true);
        updatesThread.start();
    }

    public void setCheckedSynsetId(String synsetId) {
        checkedSynsetId = synsetId;
        checkSingleSynset = true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateTask = new Task<List<SynsetUpdate>>() {
            @Override
            protected List<SynsetUpdate> call() throws IOException, InterruptedException {
                if (checkSingleSynset) {
                    SynsetUpdate update = mainController.getBoard().checkForUpdates(checkedSynsetId);
                    return update != null ? Arrays.asList(update) : new ArrayList<>();
                } else {
                    return mainController.getBoard().checkForUpdates();
                }
            }
        };
        mainPane.visibleProperty().bind(updateTask.runningProperty().not());
        progressIndicatorVB.visibleProperty().bind(updateTask.runningProperty());

        updateTask.setOnSucceeded(workerStateEvent -> showUpdates());
        updateTask.setOnFailed(workerStateEvent -> Utils.showError(updateTask.getException().getMessage()));

        addedEdgeFromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        addedEdgeToColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        addedEdgeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        addedEdgeWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        removedEdgeFromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        removedEdgeToColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        removedEdgeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        removedEdgeWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        removedSynsetIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        removedSynsetRepresentationColumn.setCellValueFactory(new PropertyValueFactory<>("representation"));
        removedSynsetDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        addedEdges = FXCollections.observableArrayList();
        removedEdges = FXCollections.observableArrayList();
        removedSynsets = FXCollections.observableArrayList();
        editedSynsets = FXCollections.observableArrayList();
        editedEdges = FXCollections.observableArrayList();

        addedEdgesTable.setItems(addedEdges);
        removedEdgesTable.setItems(removedEdges);
        removedSynsetsTable.setItems(removedSynsets);
        editedSynsetsLV.setItems(editedSynsets);
        editedEdgesLV.setItems(editedEdges);

        Callback<TableColumn<EdgeData, String>, TableCell<EdgeData, String>> addedEdgesCellFactory = createAddedEdgesCancelCell();
        Callback<TableColumn<EdgeData, String>, TableCell<EdgeData, String>> removedEdgesCellFactory = createRemovedEdgesCancelCell();
        Callback<TableColumn<Synset, String>, TableCell<Synset, String>> removedSynsetsCellFactory = createRemovedSynsetsCancelCell();

        addedEdgeCancelColumn.setCellFactory(addedEdgesCellFactory);
        removedEdgeCancelColumn.setCellFactory(removedEdgesCellFactory);
        removedSynsetCancelColumn.setCellFactory(removedSynsetsCellFactory);

        stackPane.prefWidthProperty().bind(scrollPane.widthProperty());
        stackPane.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> scrollPane.getHeight() - 10, scrollPane.heightProperty()));
    }

    public void applyUpdates() {
        mainController.getBoard().update(updates);
        mainController.getBrowserController().redrawNodes();
        Stage stage = (Stage) applyButton.getScene().getWindow();
        stage.close();
    }

    private void showUpdates() {
        updates = updateTask.getValue();
        for (SynsetUpdate update : updates) {
            if (update.getUpdatedSynset() == null) {
                removedSynsets.add(update.getOriginalSynset());
                continue;
            }

            if (update.areSynsetPropertiesUpdated()) {
                editedSynsets.add(update.getOriginalSynset());
            }

            for (Edge edge : update.getAddedEdges().values()) {
                addEdgeToObservableList(edge, update, addedEdges);
            }

            for (Edge edge : update.getRemovedEdges().values()) {
                addEdgeToObservableList(edge, update, removedEdges);
            }

            for (EdgeEdit edgeEdit : update.getEdgesToMerge().values()) {
                EdgeMergePanel mergePanel = new EdgeMergePanel(update, edgeEdit, this);
                edgeMergesVB.getChildren().add(mergePanel);
            }

            for (EdgeEdit edgeEdit : update.getEdgeEdits().values()) {
                editedEdges.add(edgeEdit.getOriginal());
            }
        }

        if (removedSynsets.isEmpty()) {
            accordion.getPanes().remove(removedSynsetsTP);
        }
        if (editedSynsets.isEmpty()) {
            accordion.getPanes().remove(editedSynsetsTP);
        }
        if (addedEdges.isEmpty()) {
            accordion.getPanes().remove(addedEdgesTP);
        }
        if (removedEdges.isEmpty()) {
            accordion.getPanes().remove(removedEdgesTP);
        }
        if (edgeMergesVB.getChildren().isEmpty()) {
            accordion.getPanes().remove(duplicateEdgesTP);
        }
        if (editedEdges.isEmpty()) {
            accordion.getPanes().remove(editedEdgesTP);
        }

        checkIfNoChangesLeft();
    }

    private void addEdgeToObservableList(Edge edge, SynsetUpdate update, ObservableList<EdgeData> list) {
        String from = update.getOriginalSynset().getRepresentation();
        String to = update.getPointedSynset(edge).getRepresentation();
        list.add(new EdgeData(from, to, edge));
    }

    private Callback<TableColumn<EdgeData, String>, TableCell<EdgeData, String>> createAddedEdgesCancelCell() {
        return new Callback<TableColumn<EdgeData, String>, TableCell<EdgeData, String>>() {
            @Override
            public TableCell<EdgeData, String> call(final TableColumn<EdgeData, String> param) {
                TableCell<EdgeData, String> cell = new TableCell<EdgeData, String>() {

                    final Button button = new Button("Cancel");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        }
                        else {
                            button.setOnAction((ActionEvent event) -> {
                                EdgeData edgeData = getTableView().getItems().get(getIndex());
                                SynsetUpdate update = updates.stream().filter(u -> u.getOriginalSynset().getId().equals(edgeData.getFromId())).findFirst().get();
                                update.cancelEdgeAddition(edgeData.getId());
                                addedEdgesTable.getItems().remove(edgeData);
                                if (addedEdges.isEmpty()) {
                                    accordion.getPanes().remove(addedEdgesTP);
                                }
                                checkIfNoChangesLeft();
                            });
                            setGraphic(button);
                            setText(null);
                        }
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        };
    }

    private Callback<TableColumn<EdgeData, String>, TableCell<EdgeData, String>> createRemovedEdgesCancelCell() {
        return new Callback<TableColumn<EdgeData, String>, TableCell<EdgeData, String>>() {
            @Override
            public TableCell<EdgeData, String> call(final TableColumn<EdgeData, String> param) {
                TableCell<EdgeData, String> cell = new TableCell<EdgeData, String>() {

                    final Button button = new Button("Cancel");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        }
                        else {
                            EdgeData edgeData = getTableView().getItems().get(getIndex());

                            // Disable canceling of edge removal if the edge source or target are going to be deleted.
                            button.disableProperty().bind(Bindings.createBooleanBinding(() ->
                                removedSynsets.stream().anyMatch(s -> s.getId().equals(edgeData.getFromId()) || s.getId().equals(edgeData.getToId())),
                                removedSynsets));

                            button.setOnAction((ActionEvent event) -> {
                                SynsetUpdate update = updates.stream().filter(u -> u.getOriginalSynset().getId().equals(edgeData.getFromId())).findFirst().get();
                                update.cancelEdgeRemoval(edgeData.getId());
                                removedEdgesTable.getItems().remove(edgeData);
                                if (removedEdges.isEmpty()) {
                                    accordion.getPanes().remove(removedEdgesTP);
                                }
                                checkIfNoChangesLeft();
                            });
                            setGraphic(button);
                            setText(null);
                        }
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        };
    }

    private Callback<TableColumn<Synset, String>, TableCell<Synset, String>> createRemovedSynsetsCancelCell() {
        return new Callback<TableColumn<Synset, String>, TableCell<Synset, String>>() {
            @Override
            public TableCell<Synset, String> call(final TableColumn<Synset, String> param) {
                TableCell<Synset, String> cell = new TableCell<Synset, String>() {

                    final Button button = new Button("Cancel");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        }
                        else {
                            button.setOnAction((ActionEvent event) -> {
                                Synset synset = getTableView().getItems().get(getIndex());
                                SynsetUpdate update = updates.stream().filter(u -> u.getOriginalSynset().getId().equals(synset.getId())).findFirst().get();
                                updates.remove(update);
                                removedSynsetsTable.getItems().remove(synset);
                                if (removedSynsets.isEmpty()) {
                                    accordion.getPanes().remove(removedSynsetsTP);
                                }
                                checkIfNoChangesLeft();
                            });
                            setGraphic(button);
                            setText(null);
                        }
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        };
    }

    private void checkIfNoChangesLeft() {
        if (accordion.getPanes().isEmpty()) {
            stackPane.getChildren().clear();
            stackPane.getChildren().add(new Label("No updates"));
        }
    }

    public void cancelEdgeMerge(EdgeMergePanel edgeMergePanel) {
        edgeMergePanel.getSynsetUpdate().cancelEdgeReplacement(edgeMergePanel.getEdgeEdit().getId());
        edgeMergesVB.getChildren().remove(edgeMergePanel);
        if (edgeMergesVB.getChildren().isEmpty()) {
            accordion.getPanes().remove(duplicateEdgesTP);
        }
        checkIfNoChangesLeft();
    }

    public static class EdgeData {
        private final String id;
        private final String fromId;
        private final SimpleStringProperty from;
        private final String toId;
        private final SimpleStringProperty to;
        private final SimpleStringProperty type;
        private final SimpleDoubleProperty weight;

        public EdgeData(String from, String to, Edge edge) {
            this.id = edge.getId();
            this.fromId = edge.getOriginSynsetId();
            this.from = new SimpleStringProperty(from);
            this.toId = edge.getPointedSynsetId();
            this.to = new SimpleStringProperty(to);
            this.type = new SimpleStringProperty(edge.getRelationType().toString());
            this.weight = new SimpleDoubleProperty(edge.getWeight());
        }

        public String getId() {
            return id;
        }

        public String getFromId() {
            return fromId;
        }

        public String getToId() {
            return toId;
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