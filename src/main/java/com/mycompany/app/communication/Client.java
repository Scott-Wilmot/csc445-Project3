package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Packet;
import com.mycompany.app.model.Player;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Client {

    DatagramSocket client_socket;
    GameState gameState;
    int id; // id should have ranges of 0-3?

    static int PORT = 26880;
    static String HOST = "localhost";

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.connect(HOST, PORT);
    }

    public Client() throws SocketException {
        client_socket = new DatagramSocket(0);
        gameState = new GameState();
    }

    /**
     * Connects to the host of a game and receives a unique id from the host upon successful connection
     *
     * @param ip - the ip address of the host
     * @param port - the port of the host
     */
    public boolean connect(String ip, int port) throws IOException {
        byte[] msg = Packet.createJoinPacket((short) 0, (short) 0);
        DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName(ip), port);

        client_socket.setSoTimeout(200);

        client_socket.send(packet);
        try {
            client_socket.receive(packet);
            Packet data = Packet.processJoinPacket(packet.getData());
            id = data.id; // Yippeeeeeee should be binded and connected now
            return true;
        } catch (SocketTimeoutException t) {
            System.out.println("Timeout");
            return false;
        } finally {
            client_socket.setSoTimeout(0);
        }

    }

    /**
     * Sends the current game state to the client via UDP Packets
     * (Packet representation - opcode, block num and data)
     */
    public void send_update() throws IOException {
        Packet[] packets = Packet.createGameStatePackets(gameState);
        DatagramPacket send_buf;

        for (Packet packet : packets) {
            int packetSize = Short.SIZE + Short.SIZE + packet.data.length;
            ByteBuffer buf = ByteBuffer.allocate(packetSize);
            buf.putShort(packet.opCode);
            buf.putShort(packet.block_num);
            buf.put(packet.data);

            send_buf = new DatagramPacket(buf.array(), buf.array().length);
            client_socket.setSoTimeout(300);

            outerLoop:
            while (true) {
                client_socket.send(send_buf);
                try {
                    client_socket.receive(send_buf);
                    break outerLoop;
                } catch (SocketTimeoutException s) {
                    continue;
                }
            }

        }
    }

    /**
     * Receives GameState packets, reorders them, deserializes them and updates self GameState field to received GameState
     */
    public void receive_update() throws IOException, ClassNotFoundException {
        HashMap<Short, byte[]> map = new HashMap<>();
        int packet_size = 1024; // Arbitrary magic number, need better place to store this
        DatagramPacket packet = new DatagramPacket(new byte[packet_size], packet_size);

        while (true) {
            client_socket.receive(packet);

            ByteBuffer buf = ByteBuffer.allocate(1024); // Magic number yippeeeeee, jk fix this
            buf.put(packet.getData());
            buf.flip();
            short opCode = buf.getShort();
            short block_num = buf.getShort();
            byte[] data = new byte[buf.remaining()];
            buf.get(data);

            map.put(block_num, data);

            // Now send a meaningless ACK
            byte[] msg = "ACTG".getBytes();
            packet = new DatagramPacket(msg, msg.length);

            if (packet.getLength() < packet_size) {
                break; // The end of packets receiving, what if the ACK isn't successfully delivered?
            }
        }

        // Now Reconstruct and update GameState
        gameState = Packet.processGameStatePackets(map);
    }

    /**
     *  Wait to get data from a datagram socket
     */
    public void waiting() throws IOException {
        System.out.println("Waiting...");
        while (true){
            byte[] msg = new byte[1024];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);

            client_socket.receive(packet);
            System.out.println("Data Received.");
        }
    }


    /**
     * RAFT IMPLEMENTATION
     */
    int currentTerm = 1;
    // how long it should take before a new election is started
    int electionTimeoutMS = 3000;
    // how often the leader should send their heartbeat
    int heartbeatTimeoutMS = 1400;
    // who the client voted for (during the election)
    int vote = -1;

    // the states that these clients can be in
    enum RaftState {
        LEADER,
        CANDIDATE,
        FOLLOWER
    }
    RaftState raftState = RaftState.FOLLOWER; // there are 3 states; follower, candidate, leader
    // initially, the host starts off as leader,

    private Timer electionTimer;
    private long lastHeartbeatReceived = System.currentTimeMillis();


    /**
     * In RAFT typical, you send a heartbeat every x ms.
     * In our modified RAFT, you request a heartbeat and receive it.
     */
    public void requestHeartbeat() throws IOException {
        byte[] requestHeartbeat = Packet.createAckPacket(Packet.Opcode.HEARTBEAT, 0);
    }

    /**
     * AppendEntries RPCs - these are known as heartbeats in the RAFT protocol.
     * It is used to let the followers know that the leader is still alive.
     *
     * @throws IOException - if an I/O error occurs (should never occur)
     */
    public void sendHeartbeats() throws IOException {
        // fail condition
        if (raftState == RaftState.FOLLOWER ) {
            // maybe reset the timer too?
            return;
        }

    }

}
