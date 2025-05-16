package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;

import java.net.DatagramSocket;

public abstract class User {

    int PACKET_SIZE = 1024;
    int id;
    GameState gameState;
    DatagramSocket heartbeatSocket;

    public int getID() {
        return id;
    }

    public GameState getGameState() {
        return gameState;
    }

}
