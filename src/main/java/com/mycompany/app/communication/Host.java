package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;

public class Host {

    GameState gameState;

    Host() throws IOException {

    }

    public static void main(String[] args) throws IOException {
        Host host = new Host();
    }

    public void open_lobby() throws IOException {
        ServerSocket socketMaker = new ServerSocket(0);

        // Accept new connections until game starts or player limit reached

        socketMaker.close();
    }

    public void accept_client() {

    }

    public void start_game() {

    }

    public void update_clients() {

    }

}
