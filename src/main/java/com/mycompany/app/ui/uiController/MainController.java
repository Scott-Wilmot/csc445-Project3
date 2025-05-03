package com.mycompany.app.ui.uiController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainController {

    @FXML private Button joinRoomButton;
    @FXML private Button createRoomButton;

    @FXML private TextField joinRoomUsername;
    @FXML private TextField joinRoomCode;

    @FXML private TextField createRoomUsername;
    @FXML private TextField createRoomCode;

    @FXML
    private void handleJoinRoomClick(ActionEvent event) {
        String username = joinRoomUsername.getText();
        String roomCode = joinRoomCode.getText();
        if (validateInputs(username, roomCode)) {
            loadRoomScene(event, username, roomCode, "Uno - Joined Room");
        }
    }

    @FXML
    private void handleCreateRoomClick(ActionEvent event) {
        String username = createRoomUsername.getText();
        String roomCode = createRoomCode.getText();
        if (validateInputs(username, roomCode)) {
            loadRoomScene(event, username, roomCode, "Uno - Created Room");
        }
    }

    private boolean validateInputs(String username, String roomCode) {
        if (username == null || username.trim().isEmpty()) {
            showAlert("Missing Username", "Please enter your name before continuing.");
            return false;
        }
        if (roomCode == null || roomCode.trim().isEmpty()) {
            showAlert("Missing Room Code", "Please enter a room code before continuing.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadRoomScene(ActionEvent event, String username, String roomCode, String title) {
        try {
            URL fxmlLocation = getClass().getResource("/fxmlViews/RoomView.fxml");
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(fxmlLocation, "RoomView.fxml not found"));
            Parent root = loader.load();

            RoomViewController controller = loader.getController();
            controller.setRoomCode(roomCode);
            controller.setUsername(username); // ← Add this method if you want to use username in the next screen

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Loading Error", "Something went wrong while loading the room screen.");
        }
    }
}
