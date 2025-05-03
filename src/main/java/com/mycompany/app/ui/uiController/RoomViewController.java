package com.mycompany.app.ui.uiController;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class RoomViewController {

    @FXML
    Text roomCodeId;
    @FXML
    Text username;

    public void setRoomCode(String roomCodeId) {
        this.roomCodeId.setText(roomCodeId);
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }
}
