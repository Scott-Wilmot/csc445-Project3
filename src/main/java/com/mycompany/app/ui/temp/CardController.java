package com.mycompany.app.ui.temp;

import com.mycompany.app.model.Card;
import com.mycompany.app.model.Shape;
import com.mycompany.app.model.Value;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CardController {

    private List<Card> availableCards;

    private List<Card> userCards;

    /**
     *
     */
    public void prepareCards() {
        File dir = new File(System.getProperty("user.dir") + "/src/main/resources/cardImages");

        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            String fileName = file.getName();
            String temp =  fileName.split("\\.")[0];
            String[] li = temp.split("_");
            if (li[0].equalsIgnoreCase("b") && li[1].equalsIgnoreCase("w")) continue;
            Card card = new Card(Shape.fromString(li[0]), Value.fromString(li[1]));

        }

    }

    public static void main(String[] args) {
        CardController cardController = new CardController();
        cardController.prepareCards();
    }
}
