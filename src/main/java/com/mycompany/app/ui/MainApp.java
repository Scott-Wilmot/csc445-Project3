package com.mycompany.app.ui;

import com.mycompany.app.communication.Client;
import com.mycompany.app.communication.Host;
import com.mycompany.app.ui.uiController.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {

    private Host host;
    private Client client;

    /**
     * Runs when javafx is initialized
     * Defaults to MainView.fxml for a scene
     * @param primaryStage initial stage provided
     * @throws Exception handled by FXML Loader
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlViews/MainView.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setMainApp(this);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("445 GAME");
        primaryStage.show();
    }

    /**
     * creates the host instance
     * @return host instance
     */
    public Host initHost() throws IOException {
        host = new Host(null);
        return host;
    }

    /**
     * creates the client instance
     * @return client instance
     */
    public Client initClient() throws SocketException {
        client = new Client();
        return client;
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
