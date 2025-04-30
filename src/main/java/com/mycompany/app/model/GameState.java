package com.mycompany.app.model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * GameState class for keeping track of game information, should be shared among players after everybody has connected and game has started
 */
public class GameState implements Serializable {

    private int player_count;
    private int current_turn;
    private Deque<Card> deck;
    private Deque<Card> discard_pile;

    public GameState() {
        player_count = 0;
        current_turn = 0;
        deck = new ArrayDeque<>();
        discard_pile = new ArrayDeque<>();
    }

    /**
     * Increment player count to account for a newly joined player. If max player count is already reached (4 players) return an exception
     */
    public boolean addPlayer() {
        if (player_count < 4) {
            player_count++;
            return true;
        }
        return false;
    }

    /**
     * Increments the turn number, meant to represent a player id, and modulo's current turn to return to first player from last players turn
     */
    public void nextTurn() {
        current_turn = current_turn++ % player_count;
    }

    public int getCurrentTurn() {
        return current_turn;
    }

    public void initializeDeck() {

    }

    public void reshuffleDeck() {

    }

    /**
     * Pops the top card off of the deck and returns it for the calling player object to place the card into their own hand
     * @param card
     * @return
     */
    public Card drawCard(Card card) {
        return deck.pop();
    }

    /**
     * Adds a offered card to the discard pile
     * @param card Card object offered from player class calling this method, player MUST remove card from their own hand upon calling this method
     */
    public void discardCard(Card card) {
        discard_pile.push(card);
    }

}
