package com.mycompany.app.testingenvironment;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Player;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;

import static com.mycompany.app.testingenvironment.Config.BUFFER_SIZE;

public class TestClient {
    public static void main(String[] args) {
        GameState state = new GameState();

        state.addPlayer(0, new Player(new InetSocketAddress("localhost", 8080)));
        state.addPlayer(1, new Player(new InetSocketAddress("localhost", 8080)));
        state.addPlayer(2, new Player(new InetSocketAddress("localhost", 8080)));
        state.addPlayer(3, new Player(new InetSocketAddress("localhost", 8080)));

        state.removePlayer(1);

        System.out.println(Arrays.toString(state.getPlayers().keySet().toArray(new Integer[0])));

        System.out.println(state.getCurrentTurn());
        state.nextTurn();
        System.out.println(state.getCurrentTurn());
        state.nextTurn();
        System.out.println(state.getCurrentTurn());
        state.nextTurn();
        System.out.println(state.getCurrentTurn());
        state.nextTurn();
        System.out.println(state.getCurrentTurn());
        state.nextTurn();

    }

}
