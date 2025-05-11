package com.mycompany.app.ui.uiController;

import com.mycompany.app.model.Card;
import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Player;
import com.mycompany.app.ui.utils.CustomUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


public class RoomViewController {

    @FXML
    Text roomCodeId;
    @FXML
    Text username;
    @FXML
    Group userCardsGroup;
    @FXML
    ImageView drawPile;
    @FXML
    ImageView discardPile;
    @FXML
    Text currentPlayerId;

    GameState gameState;

    @FXML
    public void initialize() throws Exception {
        GameState game = new GameState();
        System.out.println(game.getDeck().size());

        InetAddress inetAddress = InetAddress.getByName("localhost");
        Player player = new Player(inetAddress, 8082);
        Player player2 = new Player(inetAddress, 8082);
        Player player3 = new Player(inetAddress, 8082);
        Player player4 = new Player(inetAddress, 8082);

        game.addPlayer(0, player);
        game.addPlayer(1, player2);
        game.addPlayer(2, player3);
        game.addPlayer(3, player4);


        game.startGame();
        this.gameState = game;
        updateDisplayInterface();
    }

    /**
     * Changing a different user cards since the player is changing
     */
    private void updateDisplayInterface() throws Exception {

        // Display current User ID
        currentPlayerId.setText(String.valueOf(gameState.getCurrentTurn()));

        // Current user cards
        if (userCardsGroup == null) this.userCardsGroup = new Group();
        userCardsGroup.getChildren().clear();
        for (Card card : gameState.getCurrentPlayer().getPlayerHand()) {
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


        //For Debugging
        for (Map.Entry<Integer, Player> entry : gameState.getPlayers().entrySet()) {
            Player player = gameState.getPlayers().get(entry.getKey());
            System.out.println("Key: " + entry.getKey() + ", Value: " + player.getPlayerHand());
        }


    }

    @FXML
    private void handleDrawCard(MouseEvent event) throws Exception {

        gameState.drawCard(1);
        updateDisplayInterface();
    }

    @FXML
    private void endTurn(MouseEvent event) throws Exception {
        gameState.endTurn();
        updateDisplayInterface();
    }

    /**
     * Adds a new card to the user card group and initializes its interaction behavior.
     * <p>
     * This method generates a random card image, creates a new {@code ImageView} for
     * displaying the card, and configures its behavior for interactions such as mouse
     * clicks and hover effects. When the card is clicked, it triggers the discard action.
     * Mouse hover actions move the card vertically for visual feedback.
     * <p>
     * The card is added to the {@code userCardsGroup}, and the layout of the user's cards
     * is reorganized after adding the new card.
     * <p>
     * The method catches and logs any exceptions that occur during the process.
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
                    int index = getCardIndex(cardImageLocation);
                    gameState.placeCard(gameState.getActivePlayerCard(index));
                    updateDisplayInterface();
                } catch (Exception e) {
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
        for (int i = 0; i < gameState.getCurrentPlayer().getPlayerHand().size(); i++) {
            Card card = gameState.getCurrentPlayer().getPlayerHand().get(i);
            if (cardImageLocation.equals(card.getFileName())) {
                return i;
            }
        }
        throw new Exception("Card not found");
    }


    public void setRoomCode(String roomCodeId) {
        this.roomCodeId.setText(roomCodeId);
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }


}
