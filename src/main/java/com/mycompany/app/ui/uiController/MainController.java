package com.mycompany.app.ui.uiController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainController {

    public Button myButton;

    public void handleClick(ActionEvent actionEvent) {
        System.out.println("Hello World!");
        System.out.println("This is being clicked. This is something");
    }
}
