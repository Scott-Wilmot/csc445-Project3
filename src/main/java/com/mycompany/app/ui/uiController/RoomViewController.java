package com.mycompany.app.ui.uiController;

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
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import jdk.jshell.execution.Util;

import java.util.Random;

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
    private void addNewCard() {
        try {
            System.out.println("at least it is clicked");
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

    private void discardCard(MouseEvent event, ImageView imageView) {
        discardPile.setImage(imageView.getImage());
        userCardsGroup.getChildren().remove(imageView);
        organizeUserCards();
    }

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
