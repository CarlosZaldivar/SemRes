package com.github.semres.gui;

import com.github.semres.SemRes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        primaryStage.setTitle("SemRes");
        primaryStage.setScene(new Scene(root, 600, 550));
        primaryStage.show();
    }

    @Override
    public void stop() {
        SemRes.getInstance().save();
    }
}
