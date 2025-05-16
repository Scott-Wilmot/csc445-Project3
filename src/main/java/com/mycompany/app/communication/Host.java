package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Packet;
import com.mycompany.app.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Host extends User {

    DatagramChannel host_channel;
    HashMap<Integer, Player> clients;
    boolean game_started;

    int PORT = 26880;
    String HOST = "0.0.0.0";

    public Host(String host_name) throws IOException {
        host_channel = initialize_socket(host_name);
        gameState = new GameState();
        id = 0;

        InetSocketAddress addr = (InetSocketAddress) host_channel.getLocalAddress();
        Player player = new Player(addr);
        gameState.addPlayer(id, player); // Host id defaults to 1

        game_started = false;
        clients = new HashMap<>();
    }

    /**
     * Initializes a non-blocking DatagramChannel bound to the given hostname.
     *
     * @param host_name string representation of host name for Host Instance
     * @return A DatagramChannel instance that is bound to Host.
     */
    DatagramChannel initialize_socket(String host_name) throws IOException {
        InetSocketAddress addr;
        if (host_name.equals("localhost")) {
            addr = new InetSocketAddress(InetAddress.getByName(host_name), 0);
        } else {
            addr = new InetSocketAddress(getPublicIP(), 0);
        }
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(addr);
        return channel;
    }

    /**
     * Opens the game lobby, allowing clients to join before the game starts.
     * Registers each client with a unique ID and responds to join requests with an acknowledgment packet.
     * Currently accepts any incoming message as a join request (OpCode check is a TODO).
     */
    public void open_lobby() throws IOException, InterruptedException {
        int player_count = 1; // Defaults to 1, accounting for host
        ByteBuffer buf = ByteBuffer.allocate(4);

        // This should not be the only method for holding off game start, since the loop terminates as soon as 4 players are in
        while (player_count < 4 && !game_started) {
            buf.clear();
            SocketAddress addr = host_channel.receive(buf);
            buf.flip();

            if (addr != null) {
                InetSocketAddress ip = (InetSocketAddress) addr;
                int port = ((InetSocketAddress) addr).getPort();

                Player player = new Player(ip);
                int playerId = player_count;
                clients.put(playerId, player);
                gameState.addPlayer(playerId, player);

                SocketAddress target = new InetSocketAddress(ip.getAddress(), port);
                buf.clear();
                buf.put(Packet.createJoinPacket((short) 0, (short) player_count)); // Arbitrary 0 OpCode for join packets
                buf.flip();
                host_channel.send(buf, target);
                player_count++;
            }

            Thread.sleep(100);
        }
    }

    /**
     * Alerts all connected clients of the games start. Should send alerts and wait for an ACK from each client before starting game on hosts side.
     * Needs to also communicate an initial gamestate that the host generates on its end -> this method initializes gamestate?
     */
    public void send_start_alert() throws IOException {
        startGame(); // Ends the open_lobby() thread if still running
        gameState.startGame();
        ByteBuffer buf = ByteBuffer.allocate(Short.SIZE);
        buf.putShort((short) 1); // 1 for start OpCode

        Set<Integer> keys = clients.keySet();
        for (Integer key : keys) {
            Player player = clients.get(key);
            if (player.getID() != id) { // Skips host start message since we don't need it
                host_channel.send(buf, clients.get(key).getSocketAddress());
                host_channel.receive(buf);
            }
        }
    }

    /**
     * Sends the current game state to all connected clients.
     * <p>
     * The game state is split into packets, each sent to every client.
     * Retries sending if no acknowledgment is received (simple resend loop).
     */
    public void update_clients() throws IOException, InterruptedException {
        int encryption = 390;
        ArrayList<byte[]> packets = (ArrayList<byte[]>) Packet.createGamePackets(encryption, gameState);

        for (byte[] packet : packets) {
            // Place packet information into a byte array
            ByteBuffer buf = ByteBuffer.wrap(packet);

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
                        while (host_channel.receive(recv) == null) {
                            System.out.println("No packet recvd");
                            Thread.sleep(timeout);
                        }
                        System.out.println("Packet received");
                    }
                }
            }
        }
    }

    /**
     * Receives a new GameState update from a client.
     */
    public void receive_update() throws IOException, ClassNotFoundException {
        ByteBuffer buf = ByteBuffer.allocate(PACKET_SIZE);
        HashMap<Short, byte[]> packets = new HashMap<>();

        while (true) {
            buf.clear();
            SocketAddress addr = host_channel.receive(buf);

            if (addr != null) {
                System.out.println("BUF REMAINING SIZE: " + buf.remaining());
                short opCode = buf.getShort();
                short blockNum = buf.getShort();
                byte[] data = new byte[Packet.DATA_SIZE];
                System.out.println("ERROR? " + buf.remaining());
                buf.get(data); // BUFFER UNDERFLOW RIGHT here
                System.out.println("NO ERROR");

                packets.put(blockNum, data);

                ByteBuffer ack = ByteBuffer.allocate(Short.SIZE / 8);
                ack.putShort(opCode); // OpCode the same to help confirm the correct ack for sender
                System.out.println("Sending ACK");
                host_channel.send(ack, addr);
                System.out.println("ACK SENT");

                System.out.println("Remaining buf size: " + buf.remaining());
                if (buf.remaining() < PACKET_SIZE) {
                    System.out.println("BREAK CONDITION!!!");
                    System.out.println(buf.remaining() + " < " + PACKET_SIZE);
                    break;
                }
            }
        }

        System.out.println("Exiting receive");
        gameState = Packet.processGameStatePackets(packets);
    }

    public String getPublicIP() {
        try {
            URL url = new URL("https://api.ipify.org"); // Simple IP response
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String publicIP = in.readLine();
                return publicIP;
            }

        } catch (Exception e) {
            System.err.println("Could not determine public IP address.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the local IP address bound to the host's DatagramChannel.
     *
     * @return The local IP address as a string.
     * @throws IOException if an I/O error occurs while retrieving the address.
     */
    public String getLocalAddress() throws IOException {
        return ((InetSocketAddress) host_channel.getLocalAddress()).getAddress().getHostAddress();
    }

    /**
     * Retrieves the local IP port bound to the host's DatagramChannel.
     *
     * @return The local IP port as a string.
     * @throws IOException if an I/O error occurs while retrieving the port.
     */
    public String getLocalPort() throws IOException {
        int port = ((InetSocketAddress) host_channel.getLocalAddress()).getPort();
        return String.valueOf(port);
    }

    public void startGame() {
        game_started = true;
    }

}
