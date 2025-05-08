package com.mycompany.app.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    int id;
    List<Card> playerHand;
    InetAddress playerAddress;
    int port;

    boolean cardDrawn;
    boolean cardPlayed;

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

    public boolean hasDrawnCard() {
        return cardDrawn;
    }

    public void hasDrawnCard(boolean drawn) {
        cardDrawn = drawn;
    }

    public boolean hasPlayedCard() {
        return cardPlayed;
    }

    public void hasPlayedCard(boolean cardPlayed) {
        this.cardPlayed = cardPlayed;
    }

    // used to determine if the player can stack the +2s/+4s/
    public boolean hasDrawCard(Card playedCard) {
        if (playedCard.shape() == Shape.DRAW_FOUR) {
            for (Card card : playerHand) {
                if (card.shape() == Shape.DRAW_FOUR) {
                    return true;
                }
            }
            return false;
        }

        if (playedCard.shape() == Shape.DRAW_TWO) {
            for (Card card : playerHand) {
                if (card.shape() == Shape.DRAW_TWO) {
                    return true;
                }
            }
            return false;
        }
        return false;
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
