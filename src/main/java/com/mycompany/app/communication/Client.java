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
    static String HOST = "129.3.20.24";


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
    int electionTimeoutMS = ThreadLocalRandom.current().nextInt(2000,4000);
    // who the client voted for (during the election)
    int vote = -1;
    // used to see if we receieved the heartbeat
    boolean heartbeatReceived = true;

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
        byte[] heartbeat = Packet.createAckPacket(Packet.Opcode.HEARTBEAT, currentTerm);
        for (Player players : gameState.getPlayers().values()) {
            DatagramPacket heartbeatPacket = new DatagramPacket(heartbeat, heartbeat.length, players.getAddress(), players.getPort());
            client_socket.send(heartbeatPacket);
        }
    }

    /**
     * start the timer to determine if any host has disconnected;
     * sets the condition for when elections should be held
     * </p>
     * How does it work?
     * 1. You cancel a timer that may have existed before. (This means that we received the heartbeat)
     * 2. You create a timer that tells it to hold an election, if the heartbeat times out.
     * 3. If timer doesn't go off, we repeat 1-2 forever.
     * 4. If timer goes off, then we need a new election to elect a new leader. (handleElectionTimeout)
     */
    private void startRaftTimer() {
        if (electionTimer != null) {
            electionTimer.cancel();
        }

        electionTimer = new Timer(true);
        heartbeatReceived = false;

        // Schedule the timer to run repeatedly to check for heartbeat
        electionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                // Follower checks for heartbeat timeout
                if (raftState == RaftState.FOLLOWER) {
                    try {
                        requestHeartbeat();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // maybe i should do this when I receive the heartbeat at the switch-case
                    if (heartbeatReceived) {
                        stopRaftTimer();
                    }

                    // Check if enough time has passed without a heartbeat
                    // we need to implement updating this when we receive a heartbeat, which should be added to the main switch-case
                    if (currentTime - lastHeartbeatReceived > electionTimeoutMS) {
                        handleElectionTimeout();
                    }
                }
            }
        }, 0, 500); // Check every 500ms; tune as needed
    }

    /**
     * stop the timer once you received the heartbeat
     */
    private void stopRaftTimer() {
        if (electionTimer != null) {
            electionTimer.cancel();
            electionTimer = null;
        }
    }

    /**
     * A helper class for creating a new election.
     * It makes sure that the leader is not creating a new election.
     */
    private void handleElectionTimeout() {
        if (raftState != RaftState.LEADER) {
            System.out.println("Election timeout occurred. Starting new election.");
            holdRAFTElection(); // Transition to Candidate and start election
        }
    }

    /**
     * Finally, after startRaftTimer runs out, and handleElectionTimeout succeeds, we start a new election.
     * 1. Change state from Follower to Candidate.
     * 2. Increase the term.
     * 3. Vote for yourself.
     * 4. draw the rest of the owl
     */
    public void holdRAFTElection() {
        raftState = RaftState.CANDIDATE;
        currentTerm++;
        vote = this.id;
        int majority = (gameState.getPlayers().size() / 2) + 1;
        System.out.println("Node " + id + " starting election for term " + currentTerm);

//        byte[] voteRequest = Packet.createRequestVotePacket((short) currentTerm, (short) id);
    }

}
