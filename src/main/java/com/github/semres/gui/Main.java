package com.github.semres.gui;

import com.github.semres.DatabasesManager;
import com.github.semres.SemRes;
import com.github.semres.babelnet.BabelNetManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Main extends Application {
    private MainController mainController;
    private DatabasesManager databasesManager;
    private BabelNetManager babelNetManager;

    private final static Logger logger = Logger.getLogger(SemRes.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            databasesManager = new DatabasesManager();
        } catch (IOException e) {
            logger.error("Could not load settings from conf.yaml", e);
        }
        babelNetManager = new BabelNetManager();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();
        mainController = fxmlLoader.getController();
        mainController.setBabelNetManager(babelNetManager);
        mainController.setDatabasesManager(databasesManager);
        primaryStage.setTitle("SemRes");
        primaryStage.setScene(new Scene(root, 600, 550));
        primaryStage.show();
    }

    @Override
    public void stop() {
        databasesManager.save();
        mainController.close();
    }
}
