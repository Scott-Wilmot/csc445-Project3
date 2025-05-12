package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Packet;
import com.mycompany.app.model.Player;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Set;

public class Host {

    DatagramChannel host_channel;
    GameState gameState;
    HashMap<Integer, Player> clients;
    boolean game_started;

    public Host(String host_name) throws IOException {
        host_channel = initialize_socket(host_name);
        game_started = false;
        clients = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Host host = new Host(InetAddress.getLocalHost().getHostName());
        host.open_lobby();
    }

    DatagramChannel initialize_socket(String host_name) throws IOException {
        InetSocketAddress addr;
        if (host_name == "localhost") {
            addr = new InetSocketAddress(InetAddress.getByName(host_name), 0);
        } else {
            addr = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), 0);
        }
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(addr);
        return channel;
    }

    /**
     * Initializes the lobby for clients to join the game, registers clients in the host code so the host can process future messages and relate them to a client,
     * sends a unique id to each client upon receiving a join request
     * Kinda ignores the join message right now, should in the future check that the OpCode is the join OpCode. Received data is meaningless and can be ignored
     * @throws IOException
     * @throws InterruptedException
     */
    public void open_lobby() throws IOException, InterruptedException {
        System.out.println("local_addr: " + host_channel.getLocalAddress());
        int player_count = 1; // Defaults to 1, accounting for host
        ByteBuffer buf = ByteBuffer.allocate(4);

        // This should not be the only method for holding off game start, since the loop terminates as soon as 4 players are in
        while (player_count < 4 && !game_started) {
            buf.clear();
            SocketAddress addr = host_channel.receive(buf);
            buf.flip();

            if (addr != null) {
                System.out.println("Origin address: " + addr);
                InetSocketAddress ip = (InetSocketAddress) addr;
                int port = ((InetSocketAddress) addr).getPort();
                clients.put(player_count++, new Player(ip.getAddress(), port));

                SocketAddress target = new InetSocketAddress(ip.getAddress(), port);
                buf.clear();
                buf.put(Packet.createJoinPacket((short) 0, (short) player_count)); // Arbitrary 0 OpCode for join packets
                buf.flip();
                host_channel.send(buf, target);
            }

            Thread.sleep(100);
        }

        System.out.println("Player count reached or game started");
    }

    /**
     * For each packet GameState has been split into, send the packet to each client socket
     */
    public void update_clients() throws IOException, InterruptedException {
        Packet[] packets = Packet.createGameStatePackets(gameState);

        for (Packet packet : packets) {
            // Place packet information into a byte array
            int packetSize = Short.SIZE + Short.SIZE + packet.data.length;
            ByteBuffer buf = ByteBuffer.allocate(packetSize);
            buf.putShort(packet.opCode);
            buf.putShort(packet.block_num);
            buf.put(packet.data);

            // Now send byte array through the socket
            Set<Integer> keys = clients.keySet();
            for (Integer key : keys) {
                Player player = clients.get(key);
                SocketAddress addr = new InetSocketAddress(player.getAddress(), player.getPort());

                int retries = 3; // how many times to listen before giving up
                int timeout = 100; // in ms
                ByteBuffer recv = ByteBuffer.allocate(4);
                while (recv.position() == 0) { // Send loop, resends if it gets to this part, should handle packet drops
                    host_channel.send(buf, addr);
                    for (int i = 0; i < retries && recv.position() == 0; i++) { // If recv position isn't 0 we know data has been successfully received
                        Thread.sleep(timeout);
                        host_channel.receive(recv);
                    }
                }

            }

        }
    }

    public String getLocalAddress() throws IOException {
        return ((InetSocketAddress) host_channel.getLocalAddress()).getAddress().getHostAddress();
    }

    public String getLocalPort() throws IOException {
        int port =  ((InetSocketAddress) host_channel.getLocalAddress()).getPort();
        return String.valueOf(port);
    }

}
