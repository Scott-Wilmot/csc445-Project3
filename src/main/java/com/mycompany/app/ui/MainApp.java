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
    public void start(Stage primaryStage) throws IOException {
        URL fxmlUrl = getClass().getResource("/com/mycompany/app/ui/MainView.fxml");
        System.out.println("FXML URL: " + fxmlUrl);

        Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlUrl));

        primaryStage.setTitle("Scene Builder Example");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
