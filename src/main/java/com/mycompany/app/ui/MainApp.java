package com.mycompany.app.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxmlViews/MainView.fxml")));
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Uno of soney");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
