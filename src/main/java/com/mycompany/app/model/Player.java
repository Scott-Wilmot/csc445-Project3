package com.mycompany.app.model;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Player {
    int id;
    List<Card> playerHand;
    InetAddress playerAddress;
    int port;

    boolean cardDrawn;

    public Player(InetAddress playerAddress, int port) {
        playerHand = new ArrayList<>();
        this.playerAddress = playerAddress;
        cardDrawn = false;
        this.port = port;
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(List<Card> playerHand) {
        this.playerHand = playerHand;
    }

    public void setPlayerID(int id) {
        this.id = id;
    }

    void addCard(Card card) {
        playerHand.add(card);
    }

    void removeCard(Card card) {
        playerHand.remove(card);
    }

    Card getCard(int index) {
        return playerHand.get(index);
    }

    public int getID() {
        return id;
    }

    public InetAddress getAddress() {
        return playerAddress;
    }

    public int getPort() {
        return port;
    }

}
