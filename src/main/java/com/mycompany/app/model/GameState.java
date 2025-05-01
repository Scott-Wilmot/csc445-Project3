package com.mycompany.app.model;

import java.io.Serializable;
import java.util.*;

/**
 * GameState class for keeping track of game information, should be shared among players after everybody has connected and game has started
 */
public class GameState implements Serializable {

    private int player_count;
    private int current_turn;
    private Deque<Card> deck;
    private Deque<Card> discardPile;

    /**
     *
     */
    public GameState() {
        player_count = 0;
        current_turn = 0;
        deck = new ArrayDeque<>();
        discardPile = new ArrayDeque<>();
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

    /**
     *
     */
    public void initializeDeck() {
    }

    /**
     * Transforms the discard pile into the draw deck. It is used when all the cards from the draw deck have been drawn, but the
     * game has not ended. To continue the game, you reuse the discard pile as the draw pile. Shuffling allows for more randomized
     * gameplay.
     * <p/>
     *
     * @modifies {@link #deck} by filling with the values from {@link #discardPile}. Clears {@link #discardPile}
     */
    public void reshuffleDeck() {
        List<Card> reshuffledDeck = new ArrayList<>(discardPile);
        Collections.shuffle(reshuffledDeck);

        discardPile.clear();
        deck = new ArrayDeque<>(reshuffledDeck);
    }

    /**
     * When the activeUser has one card, they are required to call 445.
     * If the activeUser calls 445 when they have one card in their hand, then the game may proceed to the next turn.
     * If the activeUser does not call 445 and another user does not catch it, then the game may proceed to the next turn.
     * If the activeUser does not call 445 and another user catches it, then the activeUser must pick up two cards to their hand.
     * <p/>
     *
     * @modifies [player] so that 445 has been set to true
     */
    public void call445() {
        // implementation halted until we know how we are managing the user class
    }

    /**
     * Works in relation to {@link #call445()}
     * If the activeUser fails to call 445 when they have one card left in their hand, then any other user may call this method.
     * When this method is called, the activeUser must pick up two cards.
     * <p/>
     *
     * @param player - the activeUser who must pick up the card
     */
    public void catchFailed445(Player player) {
        // implementation halted until we know how we are managing the user class
        // note for future: for now, we don't plan on incorrectly calling 445, so there is no need to keep track of who called it
    }

    /**
     * This is the main game flow for the game.
     * When it is the activeUser's turn, they are required to place a card that matches either the Card's color or value.
     * If the user is unable to place a valid card, they are required to pull one card from the deck.
     * &#9; If the card pulled from the deck is a valid card, they may place the card down and end their turn.
     * &#9; If the card pulled from the deck is not a valid card, they must keep the card in their hand and end their turn.
     *
     * @param player - the activeUser who is required to play this turn
     * @param card   - the card that they are placing down
     */
    public void placeCard(Player player, Card card) {
        // implementation halted until we know how we are managing the user class
        /*
        If I have valid card, I may place a card.
        else, I must pull from the deck
                if the deck gave me a valid card, i may play it
                if the deck did not give me a valid card, i must keep it
         */
    }


    /**
     * Pops the top card off of the deck and returns it for the calling player object to place the card into their own hand
     *
     * @param card
     * @return
     */
    public Card drawCard(Card card, Player player) {
        return deck.pop();
        // remember to add user logic where we add it to the correct user
    }

    /**
     * Adds a offered card to the discard pile
     *
     * @param card Card object offered from player class calling this method, player MUST remove card from their own hand upon calling this method
     */
    public void discardCard(Card card) {
        discardPile.push(card);
        // remember to add user logic where we add it to the correct user
    }

    public static void main(String[] args) {
        GameState gameState = new GameState();

    }

}
