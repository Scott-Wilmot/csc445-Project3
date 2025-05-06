package com.mycompany.app.ui.uiController;

import com.mycompany.app.model.GameState;
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

    GameState gameState;

    public RoomViewController() {
        this.gameState = new GameState();
        System.out.println(gameState.getDeck().size());
    }

    /**
     * Adds a new card to the user card group and initializes its interaction behavior.
     *
     * This method generates a random card image, creates a new {@code ImageView} for
     * displaying the card, and configures its behavior for interactions such as mouse
     * clicks and hover effects. When the card is clicked, it triggers the discard action.
     * Mouse hover actions move the card vertically for visual feedback.
     *
     * The card is added to the {@code userCardsGroup}, and the layout of the user's cards
     * is reorganized after adding the new card.
     *
     * The method catches and logs any exceptions that occur during the process.
     */
    @FXML
    private void addNewCard() {
        try {
            ImageView imageView = new ImageView();
            Image image = new Image(CustomUtils.generateRandomCard());
            imageView.setImage(image);
            imageView.setFitWidth(67);
            imageView.setFitHeight(117);
            imageView.setOnMouseClicked(event -> {
                discardCard(event, imageView);
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
     *
     * This method sets the image of the discard pile to the image of the card
     * being discarded and removes the card from the user's hand. After the card
     * is removed, the method reorganizes the layout of the remaining cards in
     * the user's hand.
     *
     * @param event the mouse event triggered when the card is clicked
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

    public void setRoomCode(String roomCodeId) {
        this.roomCodeId.setText(roomCodeId);
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }


}
