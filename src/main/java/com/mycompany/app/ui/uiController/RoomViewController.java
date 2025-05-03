package com.mycompany.app.ui.uiController;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class RoomViewController {

    @FXML
    Text roomCodeId;

    public void setRoomCode(String roomCodeId) {
        this.roomCodeId.setText(roomCodeId);
    }
}
