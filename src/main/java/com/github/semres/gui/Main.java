package com.github.semres.gui;

import com.esotericsoftware.yamlbeans.YamlException;
import com.github.semres.DatabasesManager;
import com.github.semres.Settings;
import com.github.semres.babelnet.BabelNetManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main extends Application {
    private MainController mainController;
    private DatabasesManager databasesManager;
    private BabelNetManager babelNetManager;
    private boolean startupCompleted;

    @Override
    public void start(Stage primaryStage) throws IOException {
        Settings settings;
        try {
            settings = new Settings();
        } catch (FileNotFoundException e) {
            Utils.showError("Could not find conf.yaml");
            return;
        } catch (YamlException e) {
            Utils.showError("Could not parse conf.yaml");
            return;
        }

        databasesManager = new DatabasesManager(settings);
        babelNetManager = (BabelNetManager) settings.getSources().stream()
                .filter((source) -> source instanceof BabelNetManager)
                .findFirst().orElseThrow(RuntimeException::new);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();
        mainController = fxmlLoader.getController();
        mainController.setBabelNetManager(babelNetManager);
        mainController.setDatabasesManager(databasesManager);
        primaryStage.setTitle("SemRes");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();

        startupCompleted = true;
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (startupCompleted) {
            databasesManager.save();
            mainController.dispose();
        }
    }

    public MainController getMainController() {
        return mainController;
    }
}
