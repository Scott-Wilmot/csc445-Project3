package com.mycompany.app.ui.uiController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Random;

public class RoomViewController {

    @FXML
    Text roomCodeId;
    @FXML
    Text username;
    @FXML
    Group userCardsGroup;

    @FXML
    Button test;

    @FXML
    private void addNewCard(ActionEvent event1) {

        ImageView imageView = new ImageView();
        Image image = new Image("/cardImages/A_heart2.png");
        imageView.setImage(image);
        imageView.setFitWidth(67);
        imageView.setFitHeight(117);
        imageView.setId("Testing" + new Random().nextInt(100000));
        imageView.setOnMouseClicked(event -> {
            System.out.println(imageView.getId());
        });
        imageView.setOnMouseEntered(event -> {
            imageView.setLayoutY(-20);
        });
        imageView.setOnMouseExited(event -> {
            imageView.setLayoutY(0);
        });
        imageView.setPreserveRatio(true);

        userCardsGroup.getChildren().add(imageView);

        double layoutDistance = 300.0/userCardsGroup.getChildren().size()+1;
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
