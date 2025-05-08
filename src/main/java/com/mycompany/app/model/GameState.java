package com.mycompany.app.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * GameState class for keeping track of game information, should be shared among players after everybody has connected and game has started
 */
public class GameState implements Serializable {

    private int playerCount;
    private int currentTurn;
    private int cardStackCounter;
    private boolean stackActive;
    private boolean skipActive;
    private boolean turnOrderReversed;
    private Deque<Card> deck;
    private Deque<Card> discardPile;
    private Map<Integer, Player> players;

    private static final int NUM_CARDS_DEALT = 7;
    private static final int MAX_PLAYERS = 4;

    /**
     *
     */
    public GameState() {
        this.playerCount = 0;
        currentTurn = 0;
        cardStackCounter = 0;
        stackActive = false;
        skipActive = false;
        turnOrderReversed = false;
        initializeDeck();
        discardPile = new ArrayDeque<>();
        players = new HashMap<>();
    }

    /**
     * Creating a deck of all the possible cards.
     * 10 shapes × 7 values = 70 cards (normal)
     * Power Card = 4 x (+4 power cards) and one Wild Card
     * </p>
     *
     * @modifies deck so that it contains 180 cards in random order
     */
    private void initializeDeck() {
        ArrayList<Card> cards = new ArrayList<>();

        for (Shape shape : Shape.values()) {
            if (shape == Shape.DRAW_FOUR || shape == Shape.WILD) continue;
            for (Value value : Value.values()) {
                if (value == Value.W) continue;
                cards.add(new Card(shape, value));
            }
        }
        // Adding power cards
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Shape.DRAW_FOUR, Value.W));
        }
        cards.add(new Card(Shape.WILD, Value.W));

        System.out.println(cards.size());
        Collections.shuffle(cards);
        deck = new ArrayDeque<>(cards);
    }

    /**
     * Increment player count to account for a newly joined player. If max player count is already reached (4 players) return an exception
     */
    public boolean addPlayer(int playerId, Player player) {
        if (playerCount < MAX_PLAYERS) {
            if (players.containsKey(playerId)) {
                System.err.println("Duplicate Player Detected.");
                return false;
            }
            players.put(playerId, player);
            playerCount++;
            return true;
        }
        System.err.println("Max Players Reached");
        return false;
    }

    /**
     * Remove player from the game. Decrements playerCount by 1.
     * A player may leave at any point in the game. In all cases, this method's behavior will not change.
     *
     * @param playerId - the player exiting the game
     */
    private void removePlayer(int playerId) {
        players.remove(playerId);
        playerCount--;
    }

    /**
     * Initializes the game state.
     * In other words, deals the deck {@link #dealDeck()}, adds a non-wild card to the discard pile.
     */
    public void startGame() {
        dealDeck();
        // TODO: error handling: what if the first card is a +2
        // todo: complete placing first before moving forward with this
        discardPile.add(deck.removeFirst());

        // start the game with a random player
        currentTurn = ThreadLocalRandom.current().nextInt(playerCount);
    }

    /**
     * Determine the next player's turn.
     * Normally, it would increment the player turn by one and repeat in a cycle.
     * If a reverse card has been played, then it decrements the player turn and repeats in a cycle.
     */
    public void nextTurn() {
        if (turnOrderReversed) {
            currentTurn = (currentTurn - 1) % playerCount;
            if (currentTurn == -1) currentTurn = playerCount - 1;
        } else {
            currentTurn = (currentTurn + 1) % playerCount;
        }
        initializeTurn();
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public Player getCurrentPlayer() {
        return players.get(currentTurn);
    }

    /**
     * Deal cards to a single player.
     * <p/>
     *
     * @param playerID - the player retrieving the deck.
     */
    public void dealDeck(int playerID) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < NUM_CARDS_DEALT; i++) {
            cards.add(deck.removeFirst());
        }
        players.get(playerID).setPlayerHand(cards);
    }

    /**
     * Deal cards to every player in the game.
     * Used for game start.
     */
    public void dealDeck() {
        List<Card> cards = new ArrayList<>();
        for (Player player : players.values()) {
            for (int i = 0; i < NUM_CARDS_DEALT; i++) {
                cards.add(deck.removeFirst());
            }
            player.setPlayerHand(new ArrayList<>(cards));
            cards.clear();
        }
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
     * When the activePlayer has one card, they are required to call 445.
     * If the activePlayer calls 445 when they have one card in their hand, then the game may proceed to the next turn.
     * If the activePlayer does not call 445 and another player does not catch it, then the game may proceed to the next turn.
     * If the activePlayer does not call 445 and another player catches it, then the activePlayer must pick up two cards to their hand.
     * <p/>
     *
     * @modifies [player] so that 445 has been set to true
     */
    public void call445() {
        // implementation halted until we know how we are managing the player class
    }

    /**
     * Works in relation to {@link #call445()}
     * If the activePlayer fails to call 445 when they have one card left in their hand, then any other player may call this method.
     * When this method is called, the activePlayer must pick up two cards.
     * <p/>
     *
     * @param player - the activePlayer who must pick up the card
     */
    public void catchFailed445(Player player) {
        // implementation halted until we know how we are managing the user class
        // note for future: for now, we don't plan on incorrectly calling 445, so there is no need to keep track of who called it
    }

    /**
     * This is the main game flow for the game.
     * When it is the activePlayer's turn, they are required to place a card that matches either the Card's color or value.
     * If the activePlayer is unable to place a valid card, they are required to pull one card from the deck.
     * &#9; If the card pulled from the deck is a valid card, they may place the card down and end their turn.
     * &#9; If the card pulled from the deck is not a valid card, they must keep the card in their hand and end their turn.
     *
     * @param card - the card that they are placing down
     */

    // LOGIC:
    // Wild Card: Complete
    // Reverse: 1/2 complete
    // +2: TODO
    public void placeCard(Card card) {
        // fail conditions:
        // 1. Not valid card
        // but if it's a wild card, not a fail condition
        // 2. Not your turn
        System.out.println(card.value() + ", " + card.shape());
        if (card.shape() != discardPile.peekLast().shape() && card.value() != discardPile.peekLast().value()) {
            if (card.value() != Value.W && card.shape() != Shape.WILD) {
                System.out.println("Not a valid card");
                return;
            }

        }

        // pass conditions
        if (card.shape() == discardPile.peekLast().shape()
                || card.value() == discardPile.peekLast().value()
                || card.shape() == Shape.WILD
                || card.value() == Value.W) {
            discardPile.addLast(card);
            players.get(currentTurn).removeCard(card);

            // maybe take care of special conditions after the initial card is added
            if (card.shape() == Shape.DRAW_TWO) {
                // add logic for +2
                cardStackCounter += 2;
                stackActive = true;
            } else if (card.shape() == Shape.DRAW_FOUR) {
                cardStackCounter += 4;
                stackActive = true;
            } else if (card.shape() == Shape.REVERSE) {
                turnOrderReversed = !turnOrderReversed;
                // add logic to allow the player to play another card since it reverses back to them

            } else if (card.shape() == Shape.SKIP) {
                // When the activePlayer plays a skip card, the next player's turn is skipped.
                skipActive = true;

            }
        }

        // win condition
        if (players.get(currentTurn).getPlayerHand().isEmpty()) {

        }

        // end turn
        endTurn();
    }

    /**
     * Pops the top card(s) off of the deck and returns it for the calling player object to place the card into their own hand
     *
     * @param drawAmount - the number of cards to draw (can only be 1 or multiples of 2)
     * @modifies {@link Player}'s hand by adding drawAmount card
     */
    public void drawCard(int drawAmount) {

        if (drawAmount == 1) {
            players.get(currentTurn).addCard(deck.removeFirst());
            endTurn();
            return;
        }
        for (int i = 0; i < drawAmount; i++) {
            players.get(currentTurn).addCard(deck.removeFirst());
        }
    }

    /**
     * The game retrieves the active player's card.
     * Used to access any methods that require a player card.
     *
     * @param index - the card that the player chooses from their hand
     */
    public Card getActivePlayerCard(int index) {
        return players.get(currentTurn).getCard(index);
    }

    /**
     * Ends the active player's turn and passes it on to the next player.
     * Can be used by player to end their turn.
     * Accessed by methods after player action such as {@link #placeCard(Card)}.
     */
    void endTurn() {
        nextTurn();
        initializeTurn();
    }

    /**
     * Process everything before the start of a player's turn.
     */
    // TODO: add +2 stacking. currently, you can't stack.
    public void initializeTurn() {

        // 1: pick up cards
        if (stackActive) {
            // ask the user if they want to place a +2 if they have it in thier hand
            // if they say yes, place it and end turn
            // if not, pick up cards and end turn.
//            if (players.get(currentTurn).getPlayerHand().contains()) {}
            drawCard(cardStackCounter);
            stackActive = false;

            endTurn();
        } else if (skipActive) {
            // 2: skip cards
            skipActive = false;
            endTurn();
        }
    }

    private void resetGame() {
        initializeDeck();
        discardPile = new ArrayDeque<>();
    }


    // TESTING
    public Deque<Card> getDeck() {
        return deck;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public Deque<Card> getDiscardPile() {
        return discardPile;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public static void main(String[] args) throws UnknownHostException {
        GameState game = new GameState();
        System.out.println(game.getDeck().size());

        InetAddress inetAddress = InetAddress.getByName("localhost");
        Player player = new Player(inetAddress, 8082);
        Player player2 = new Player(inetAddress, 8082);
        Player player3 = new Player(inetAddress, 8082);
        Player player4 = new Player(inetAddress, 8082);

        game.addPlayer(0, player);
        game.addPlayer(1, player2);
        game.addPlayer(2, player3);
        game.addPlayer(3, player4);


        game.startGame();
        System.out.println(player.getPlayerHand());
        System.out.println(player2.getPlayerHand());
        System.out.println(player3.getPlayerHand());
        System.out.println(player4.getPlayerHand());
        System.out.println("Discard Pile " + game.getDiscardPile());

        Scanner scanner = new Scanner(System.in);
        System.out.println("Game Started: " + game.getCurrentTurn());
        String input = "";
        while (!input.equals("quit")) {
            System.out.println("PLayer " + game.getCurrentTurn() + "'s turn");
            System.out.println("ID: " + game.getCurrentPlayer().id);
            input = scanner.nextLine();
            if (input.equals("draw")) {
                game.drawCard(1);
                System.out.println();
            }

            if (input.equals("place")) {
                int index = Integer.parseInt(scanner.nextLine());
                game.placeCard(game.getActivePlayerCard(index));
            }

            if (input.equals("skip")) {
                game.endTurn();
            }

            if (input.equals("debug")) {
                System.out.println(player.getPlayerHand());
                System.out.println(player2.getPlayerHand());
                System.out.println(player3.getPlayerHand());
                System.out.println(player4.getPlayerHand());
                System.out.println("Discard Pile " + game.getDiscardPile());
            }
        }
    }
}
