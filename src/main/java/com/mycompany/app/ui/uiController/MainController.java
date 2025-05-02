package com.mycompany.app.ui.uiController;

import com.mycompany.app.ui.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    public Button myButton;

    @FXML
    public void handleClick(ActionEvent actionEvent) throws Exception {
        SceneSwitcher sceneSwitcher = new SceneSwitcher((Stage) myButton.getScene().getWindow());
        sceneSwitcher.switchScene("/fxmlViews/TestView.fxml");
    }
}
