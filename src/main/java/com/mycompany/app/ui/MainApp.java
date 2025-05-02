package com.mycompany.app.ui;

import javafx.application.Application;

public  class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) {
        primaryStage.setTitle("JavaFX test");
        primaryStage.setScene(new javafx.scene.Scene(new javafx.scene.layout.VBox(), 300, 275));
        primaryStage.show();
    }
}
