package com.mycompany.app.ui.uiController;

import com.mycompany.app.communication.Client;
import com.mycompany.app.communication.Host;
import com.mycompany.app.ui.MainApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.Objects;

public class MainController {

    @FXML
    private Text ip;
    @FXML
    private Text port;
    @FXML
    private Button createRoomButton;
    @FXML
    private Button startGameButton;


    // For joining
    @FXML
    private TextField joinRoomUsername;
    @FXML
    private TextField joinRoomCode;
    @FXML
    private Button joinRoomButton;

    MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * triggers joinRoomButton (onAction)
     *
     * @param event default action provided by javafx
     */
    @FXML
    private void handleJoinRoomClick(ActionEvent event) {
        String ip = joinRoomUsername.getText();
        int port = Integer.parseInt(joinRoomCode.getText());
        if (validateInputs(ip, String.valueOf(port))) {
//            loadRoomScene(event, ip, String.valueOf(port), "Uno - Joined Room");
            joinRoomButton.setDisable(true);

            new Thread(() -> {
                try {
                    Client client = mainApp.initClient();
                    client.connect(ip, port); // make connect a boolean to indicate a success or failure to then handle if the button turns back on or not
                    System.out.println("connected");
                    return;
                } catch (Exception e) {
                    joinRoomButton.setDisable(false);
                }
            }).start();

            showAlert("Join Failure", "Failed to connect to Host");
        }
    }

    /**
     * triggers createRoomClick (onAction)
     * @param event default action provided by javafx
     */
    @FXML
    private void handleCreateRoomClick(ActionEvent event) throws IOException {
        createRoomButton.setDisable(true);
        Host host = mainApp.initHost();
        ip.setText(host.getLocalAddress());
        port.setText(host.getLocalPort());

        new Thread(() -> {
            try {
                host.open_lobby();
                createRoomButton.setDisable(false);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        System.out.println("Starting game");
    }

    /**
     * Validate the input for the client
     *
     * @param ip   the ip address of the host
     * @param port the port of the host
     * @return true if all checks passes, else false
     */
    private boolean validateInputs(String ip, String port) {
        if (ip == null || ip.trim().isEmpty()) {
            showAlert("Missing ip", "Please enter ip before continuing.");
            return false;
        }
        if (port == null || port.trim().isEmpty()) {
            showAlert("Missing port", "Please enter a port before continuing.");
            return false;
        }
        return true;
    }

    /**
     * Displays a warning alert dialog with the given title and message. <br/>
     * Primarily used by {@link #validateInputs(String, String)}
     * @param title
     * @param message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Loads the room scene by navigating to the RoomView.fxml and setting up the
     * required parameters such as username and room code.
     */
    private void loadRoomScene(ActionEvent event, String username, String roomCode, String title) {
        try {
            URL fxmlLocation = getClass().getResource("/fxmlViews/RoomView.fxml");
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(fxmlLocation, "RoomView.fxml not found"));
            Parent root = loader.load();

            RoomViewController controller = loader.getController();
            controller.setRoomCode(roomCode);
            controller.setUsername(username);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Loading Error", "Something went wrong while loading the room screen.");
        }
    }
}
