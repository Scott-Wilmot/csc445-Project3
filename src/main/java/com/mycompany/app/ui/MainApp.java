package com.mycompany.app.ui;

import com.mycompany.app.communication.Client;
import com.mycompany.app.communication.Host;
import com.mycompany.app.communication.User;
import com.mycompany.app.ui.uiController.MainController;
import com.mycompany.app.ui.uiController.RoomViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;

public class MainApp extends Application {

    private User user;

    private RoomViewController roomController;
    private Stage primaryStage;

    /**
     * Runs when javafx is initialized
     * Defaults to MainView.fxml for a scene
     * @param stage initial stage provided
     * @throws Exception handled by FXML Loader
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // MainView screen initialization
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxmlViews/MainView.fxml"));
        Parent mainRoot = mainLoader.load();
        MainController mainController = mainLoader.getController();
        mainController.setMainApp(this);

        Scene scene = new Scene(mainRoot);
        primaryStage.setScene(scene);
        primaryStage.setTitle("445 GAME");
        primaryStage.show();
    }

    public void roomInit(String ip, String port) throws IOException {
        // RoomView initialization
        FXMLLoader roomLoader = new FXMLLoader(getClass().getResource("/fxmlViews/RoomView.fxml"));
        Parent roomRoot = roomLoader.load();
        RoomViewController roomController = roomLoader.getController();
        roomController.setMainApp(this);
        this.roomController = roomController;
        roomController.setIpAddress(ip);
        roomController.setPortNumber(port);

        Scene scene = new Scene(roomRoot);
        primaryStage.setScene(scene);
        primaryStage.setTitle("445 GAME");
        primaryStage.show();
    }

    /**
     * creates the host instance
     * @return host instance
     */
    public User initHost() throws IOException {
        user = new Host("localhost");
        return user;
    }

    /**
     * creates the client instance
     * @return client instance
     */
    public User initClient() throws SocketException {
        user = new Client();
        return user;
    }

    public User getUser() {
        return user;
    }

    public RoomViewController getRoomController() {
        return roomController;
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
