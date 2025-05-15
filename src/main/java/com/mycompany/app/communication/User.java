package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;

public abstract class User {

    int PACKET_SIZE = 1024;
    int id;
    GameState gameState;

    public int getID() {
        return id;
    }

    public GameState getGameState() {
        return gameState;
    }

}
