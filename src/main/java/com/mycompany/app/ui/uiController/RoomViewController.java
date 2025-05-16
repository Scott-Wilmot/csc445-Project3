package com.mycompany.app.ui.uiController;

import com.mycompany.app.communication.Client;
import com.mycompany.app.communication.Host;
import com.mycompany.app.communication.User;
import com.mycompany.app.model.Card;
import com.mycompany.app.model.GameState;
import com.mycompany.app.ui.MainApp;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class RoomViewController {

    @FXML
    Text ipAddress;
    @FXML
    Text portNumber;
    @FXML
    Group userCardsGroup;
    @FXML
    ImageView drawPile;
    @FXML
    ImageView discardPile;
    // The id of the player who is playing the game
    @FXML
    Text userId;
    // The id of the player who is in turn
    @FXML
    Text currentPlayerId;
    @FXML
    ImageView dougLeaImage;


    User user;
    GameState gameState;
    MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() throws Exception {
        animateDougLea();
    }

    /**
     * Changing a different user cards since the player is changing
     */
    public void updateDisplayInterface() throws Exception {

        // Display current User ID
        if (userId == null) userId = new Text();
        userId.setText(String.valueOf(user.getID()));
        currentPlayerId.setText(String.valueOf(gameState.getCurrentPlayer().getID()));


        // Current user cards
        if (userCardsGroup == null) this.userCardsGroup = new Group(); // Initializes group if not initialized
        userCardsGroup.getChildren().clear();   // Clears the users card group
        for (Card card : gameState.getPlayers().get(user.getID()).getPlayerHand()) {
            addNewCard(card.getFileName());
        }

        // DiscardPile
        Card lastDiscardCard = gameState.getDiscardPile().peekLast();

        if (lastDiscardCard != null) {
            Image discardPileImage = new Image("cardImages/" + lastDiscardCard.getFileName());

            discardPile.setImage(discardPileImage);
        } else {
            throw new Exception("The discard Pile is empty");
        }
    }

    /**
     * This method once started will listen constantly for the remainder of the game for any gameState updates.
     * Once an update is received, the roomView gameState is updated as well as the UI
     *
     * @throws Exception
     */
    public void startListening() throws Exception {

        Task task = createListeningTask();

        task.setOnSucceeded(event -> {
            try {
                startListening();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public Task createListeningTask() {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                while (true) {
                    if (user instanceof Host) { // Host here should technically also send new gamestate to all clients immediately after receiving
                        System.out.println("Host listening for update");
                        Host host = (Host) user;
                        host.receive_update();
                        gameState = host.getGameState();
                    } else if (user instanceof Client) {
                        System.out.println("Client listening for update");
                        Client client = (Client) user;
                        client.receive_update();
                        gameState = client.getGameState();
                    }

                    Platform.runLater(() -> {
                        try {
                            updateDisplayInterface();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                    Thread.sleep(100);
                }
            }
        };

        return task;
    }

    @FXML
    private void handleDrawCard() throws Exception {
        // If its your turn, else let them know it's not your turn
        if (gameState.getCurrentTurn() == user.getID()) {
            gameState.drawCard(1);
            endTurn();
        } else {
            System.err.println("It's not your turn");
        }
    }


    private void endTurn() throws Exception {
        gameState.endTurn();
        updateDisplayInterface();

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                System.out.println("Attempting to send update");
                if (user instanceof Host) {
                    Host host = (Host) user;
                    host.update_clients();
                } else if (user instanceof Client) {
                    Client client = (Client) user;
                    client.send_update();
                }
                System.out.println("Update sent");
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Adds a randomly generated card to the user group with click and hover behavior.
     * Triggers discard on click and reposition cards. Logs any exceptions.
     */
    private void addNewCard(String cardImageLocation) {
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("cardImages/" + cardImageLocation);

            imageView.setImage(image);
            imageView.setFitWidth(67);
            imageView.setFitHeight(117);
            imageView.setOnMouseClicked(event -> {
                try {
                    if (gameState.getCurrentTurn() == user.getID()) {
                        int index = getCardIndex(cardImageLocation);
                        gameState.placeCard(gameState.getPlayers().get(user.getID()).getPlayerHand().get(index)); // HERE
                        endTurn();
                    } else {
                        System.err.println("It's not your turn");
                    }
                } catch (Exception e) {
                    System.err.println("BAD CARD FILE LOCATION: " + cardImageLocation);
                    throw new RuntimeException(e);
                }

            });
            imageView.setOnMouseEntered(event -> {
                imageView.setLayoutY(-20);
            });
            imageView.setOnMouseExited(event -> {
                imageView.setLayoutY(0);
            });
            imageView.setPreserveRatio(true);
            userCardsGroup.getChildren().add(imageView);

            organizeUserCards();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Discards a card from the user's hand and updates the discard pile.
     * <p>
     * This method sets the image of the discard pile to the image of the card
     * being discarded and removes the card from the user's hand. After the card
     * is removed, the method reorganizes the layout of the remaining cards in
     * the user's hand.
     *
     * @param event     the mouse event triggered when the card is clicked
     * @param imageView the ImageView representing the card to be discarded
     */
    private void discardCard(MouseEvent event, ImageView imageView) {
        discardPile.setImage(imageView.getImage());
        userCardsGroup.getChildren().remove(imageView);
        organizeUserCards();
    }

    /**
     * Aligns the cards in the userCardsGroup evenly along the horizontal axis.
     */
    private void organizeUserCards() {
        double layoutDistance = 300.0 / userCardsGroup.getChildren().size() + 1;
        double currentlayOutDistance = layoutDistance;
        for (Node node : userCardsGroup.getChildren()) {
            node.setLayoutX(currentlayOutDistance);
            currentlayOutDistance += layoutDistance;
        }
    }

    public int getCardIndex(String cardImageLocation) throws Exception {
        String temp = cardImageLocation.split("\\.")[0];
        String[] li = temp.split("_");
        List<Card> playerHand = gameState.getPlayers().get(user.getID()).getPlayerHand();
        for (int i = 0; i < playerHand.size(); i++) { // HERE
            Card card = playerHand.get(i);
            if (cardImageLocation.equals(card.getFileName())) {
                return i;
            }
        }
        throw new Exception("Card not found");
    }


    public void setIpAddress(String ipAddress) {
        this.ipAddress.setText(ipAddress);
    }

    public void setPortNumber(String portNumber) {
        this.portNumber.setText(portNumber);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private final Random random = new Random();

    private void animateDougLea() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), dougLeaImage);
        fadeOut.setFromValue(1.0);  // fully visible
        fadeOut.setToValue(0.0);    // fully transparent
        fadeOut.play();
    }
}
