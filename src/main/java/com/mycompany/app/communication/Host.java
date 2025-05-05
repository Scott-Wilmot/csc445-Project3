package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Player;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Host {

    GameState gameState;
    HashMap<Integer, Player> clients;
    boolean game_started;

    Host() throws IOException {
        game_started = false;
    }

    public static void main(String[] args) throws IOException {
        Host host = new Host();
        host.open_lobby();
    }

    public void open_lobby() throws IOException {
        DatagramSocket host_socket = new DatagramSocket(0);
        DatagramChannel channel = host_socket.getChannel();
        channel.configureBlocking(false);

        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

        // Open socket on host
        System.out.println("IP Address: " + InetAddress.getLocalHost() + ", Port:" + host_socket.getLocalPort());

        int player_count = 1; // Default 1 for the host being a player
        while (player_count <= 4) { // Later change this to a boolean tracking if the game has been started or not
            host_socket.receive(packet); // Block until incoming join request, might have to make this nonBlocking so the loop can be exited
            System.out.println(new String(packet.getData(), StandardCharsets.UTF_8));

            // Store the clients information into a <id, Player> HashMap
            InetAddress address = packet.getAddress();
            int player_id = player_count++;
            Player player = new Player(address, packet.getPort());
            clients.put(player_id, player);

            // Send to new player their id
            byte[] buf = ByteBuffer.allocate(4).putInt(player_id).array();
            host_socket.send(new DatagramPacket(buf, buf.length, player.getAddress(), player.getPort()));
        }

        System.out.println("No more accepted clients");
    }

    public void start_game() {

    }

    public void update_clients() {

    }

}
