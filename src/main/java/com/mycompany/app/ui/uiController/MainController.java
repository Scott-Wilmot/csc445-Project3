package com.mycompany.app.ui.uiController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Objects;

public class MainController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button joinRoomButton;

    @FXML
    private Button createRoomButton;

    @FXML
    private TextField joinRoomCode;

    @FXML
    private TextField createRoomCode;

    @FXML
    public void handleJoinRoomClick(ActionEvent actionEvent) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxmlViews/RoomView.fxml")));
        root = loader.load();

        String roomCode = joinRoomCode.getText();

        RoomViewController roomViewController = loader.getController();
        roomViewController.setRoomCode(roomCode);


        Stage thisStage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        thisStage.setScene(new Scene(root));
        thisStage.setTitle("Uno - User Joined");
    }

    @FXML
    public void handleCreateRoomClick(ActionEvent actionEvent) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxmlViews/RoomView.fxml")));
        root = loader.load();

        String roomCode = createRoomCode.getText();

        RoomViewController roomViewController = loader.getController();
        roomViewController.setRoomCode(roomCode);

        Stage thisStage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        thisStage.setScene(new Scene(root));
        thisStage.setTitle("Uno - User Joined");
      }

}
