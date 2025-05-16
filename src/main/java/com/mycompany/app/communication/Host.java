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
        ByteBuffer buf = ByteBuffer.allocate(8);
        heartbeatSocket = new DatagramSocket(0);

        // This should not be the only method for holding off game start, since the loop terminates as soon as 4 players are in
        while (player_count < 4 && !game_started) {
            buf.clear();
            SocketAddress addr = host_channel.receive(buf);

            if (addr != null) {
                buf.flip();
                InetSocketAddress ip = (InetSocketAddress) addr;
                int port = ((InetSocketAddress) addr).getPort();

                Player player = new Player(ip);
                int playerId = player_count;
                clients.put(playerId, player);
                gameState.addPlayer(playerId, player);

                SocketAddress target = new InetSocketAddress(ip.getAddress(), port);
                buf.clear();
                buf.put(Packet.createJoinPacket((short) 0, (short) player_count, heartbeatSocket.getLocalPort())); // Arbitrary 0 OpCode for join packets
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
        ArrayList<Packet> packets = Packet.createGameStatePackets(gameState);

        for (Packet packet : packets) {
            // Place packet information into a byte array
            ByteBuffer buf = ByteBuffer.wrap(packet.toGameStatePacket());

            // Now send byte array through the socket
            Set<Integer> keys = clients.keySet();
            for (Integer key : keys) {
                Player player = clients.get(key);
                System.out.println("PLAYER ID: " + player.getID());
                if (player.getID() == id) {
                    continue;
                }

                SocketAddress addr = new InetSocketAddress(player.getAddress(), player.getPort());
                System.out.println("Destination address: " + addr);

                int retries = 3; // how many times to listen before giving up
                int timeout = 50; // in ms
                ByteBuffer recv = ByteBuffer.allocate(4);

                outerloop:
                while (true) { // Send loop, resends if it gets to this part, should handle packet drops
                    System.out.println("SENDBUFFER = " + packet.toGameStatePacket().length);
                    buf.rewind();
                    host_channel.send(buf, addr);

                    int i = 0;
                    while (i < retries) {
                        ByteBuffer recvBuf = ByteBuffer.allocate(4);
                        SocketAddress srcAddr = host_channel.receive(recvBuf);

                        if (srcAddr != null) {
                            System.out.println("ACK RECEIVED!!!!!!!!");
                            recvBuf.flip();
                            byte[] msg = new byte[recvBuf.remaining()];
                            recvBuf.get(msg);
                            System.out.println(Arrays.toString(msg));
                            break outerloop;
                        } else {
                            System.out.println("Nothing received");
                            i++;
                            Thread.sleep(timeout);
                        }
                    }
                }
            }
        }
    }

    /**
     * Receives a new GameState update from a client.
     */
    public void receive_update() throws IOException, ClassNotFoundException, InterruptedException {
        HashMap<Short, byte[]> packets = new HashMap<>();

        while (true) {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            SocketAddress addr = host_channel.receive(buf);

            if (addr != null) {
                buf.flip();

                if (buf.remaining() == 4) {
                    Thread.sleep(500);
                    continue;
                }

                short opCode = buf.getShort();
                short block_num = buf.getShort();
                byte[] data = new byte[buf.remaining()];
                buf.get(data);
                packets.put(block_num, data);

                ByteBuffer ack = ByteBuffer.allocate(4);
                ack.putShort(opCode);
                ack.putShort(block_num);
                host_channel.send(ack, addr);

                if (data.length < Packet.DATA_SIZE) { // End loop condition if the received packet is smaller than max packet size
                    break;
                }
            }
        }

        System.out.println("Exiting receive");
        gameState = Packet.processGameStatePackets(packets);
    }

    public void onClientDisconnect(int id) {
        gameState.removePlayer(id);
        clients.remove(id);
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
