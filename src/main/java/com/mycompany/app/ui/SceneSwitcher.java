package com.mycompany.app.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class SceneSwitcher {

    private Stage stage;

    public SceneSwitcher(Stage stage) {
        this.stage = stage;
    }

    public void switchScene(String fxml) throws Exception {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle("Uno of soney");
        stage.show();
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
