package com.mycompany.app.model;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Player {
    int playerID;
    List<Card> playerHand;
    InetAddress playerAddress;

    public Player(InetAddress playerAddress) {
        playerHand = new ArrayList<>();
        this.playerAddress = playerAddress;
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(List<Card> playerHand) {
        this.playerHand = playerHand;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

}
