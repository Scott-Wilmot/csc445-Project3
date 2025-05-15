package com.mycompany.app.ui.uiController;

import com.mycompany.app.communication.Client;
import com.mycompany.app.communication.Host;
import com.mycompany.app.communication.User;
import com.mycompany.app.ui.MainApp;
import javafx.concurrent.Task;
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

    private MainApp mainApp;

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
            joinRoomButton.setDisable(true);

            Task task = new Task() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        Client client = (Client) mainApp.initClient();
                        return client.connect(ip, port);
                    } catch (IOException e) {
                        return false; // Default to failure on error
                    }
                }
            };

            task.setOnSucceeded(success -> {
                boolean succeeded = (boolean) task.getValue();
                if (succeeded) {
                    System.out.println("Connection successful");
                    try {
                        ((Client) mainApp.getUser()).listen_for_start();
                        loadRoomScene(event, String.valueOf(ip), String.valueOf(port), "Uno - Joined Room");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    joinRoomButton.setDisable(false);
                    showAlert("Connection Error", "Failed to connect to host socket");
                }
            });

            new Thread(task).start();

            // Failure handling for fault tolerance

        }
    }

    /**
     * triggers createRoomClick (onAction)
     * @param event default action provided by javafx
     */
    @FXML
    private void handleCreateRoomClick(ActionEvent event) throws IOException {
        createRoomButton.setDisable(true);
        Host host = (Host) mainApp.initHost();
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

        new Thread(() -> {
           Host host = (Host) mainApp.getUser();
            try {
                host.send_start_alert();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        loadRoomScene(event, String.valueOf(ip), String.valueOf(port), "Uno - Joined Room");



        // Ensure open_lobby() thread ends, DONE
        // send out start game message
        // change room view, DONE

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
    private void loadRoomScene(ActionEvent event, String ip, String port, String title) {
        try {
            // Initialize the roomViewController
            mainApp.roomInit(ip, port);
            // Now load card data
            RoomViewController roomController = mainApp.getRoomController();
            User user = mainApp.getUser();
            if (user instanceof Client) {
                System.out.println("user is Client");
                // Get client instance and listen for incoming intiial gamestate
                Client client = (Client) user;
                // Receive gamestate here
                client.receive_update();
            } else if (user instanceof Host) {
                System.out.println("user is Host");
                // Send out initial gamestate
                Host host = (Host) user;
                // Send gamestate here
                host.update_clients();
            }
            roomController.setUser(user);
            roomController.setGameState(user.getGameState());
            roomController.updateDisplayInterface();
            roomController.startListening();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Loading Error", "Something went wrong while loading the room screen.");
        }
    }
}
