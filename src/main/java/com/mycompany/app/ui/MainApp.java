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
        SceneSwitcher sceneSwitcher = new SceneSwitcher(primaryStage);
        sceneSwitcher.switchScene("/fxmlViews/MainView.fxml");

//        Stage stage = new Stage();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
