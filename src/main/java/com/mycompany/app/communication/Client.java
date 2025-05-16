package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Packet;
import com.mycompany.app.model.Player;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class Client extends User {

    DatagramSocket client_socket;

    static int PORT = 26880;
    static String HOST = "129.3.20.24";

    public Client() throws SocketException {
        client_socket = new DatagramSocket(0);
    }

    /**
     * Block the Client object from futher execution until a start message is received from the host
     */
    public void listen_for_start() throws IOException {
        byte[] buf = new byte[2];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        client_socket.receive(packet);
        client_socket.send(packet);

    }

    /**
     * Connects to the host of a game and receives a unique id from the host upon successful connection
     *
     * @param ip   - the ip address of the host
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
            System.out.println("Connecting to SocketAddr: " + packet.getSocketAddress());
            client_socket.connect(packet.getSocketAddress());
            return true;
        } catch (SocketTimeoutException t) {
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
        ArrayList<Packet> packets = Packet.createGameStatePackets(gameState);

        for (Packet packet : packets) {
            byte[] data = packet.toGameStatePacket();
            DatagramPacket dataPacket = new DatagramPacket(data, data.length);

            client_socket.setSoTimeout(300);
            while (true) {
                client_socket.send(dataPacket);

                try {
                    DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);
                    client_socket.receive(ackPacket);
                    break;
                } catch (SocketTimeoutException s) {
                    System.out.println("Socket TO, resend");
                }
            }
        }

        client_socket.setSoTimeout(0);
    }

    /**
     * Receives GameState packets, reorders them, deserializes them and updates self GameState field to received GameState
     */
    public void receive_update() throws IOException, ClassNotFoundException, InterruptedException {
        HashMap<Short, byte[]> map = new HashMap<>();
        DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

        System.out.println("Begin listening for new update");
        while (true) {
            System.out.println("MR FREEZE");
            client_socket.receive(packet);

            System.out.println("CLIENT RECEIVED DATA: " + packet.getLength() + " BYTES FROM: " + packet.getSocketAddress());
            if (packet.getLength() <= 4) {
                Thread.sleep(500);
                continue;
            }

            ByteBuffer buf = ByteBuffer.wrap(packet.getData());
            short opCode = buf.getShort();
            short block_num = buf.getShort();
            byte[] data = new byte[Packet.DATA_SIZE];
            buf.get(data);

            map.put(block_num, data);

            // Now send a meaningless ACK
            byte[] ack = "ACTG".getBytes();
            DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);
            client_socket.send(ackPacket);

            if (packet.getLength() < PACKET_SIZE) {
                break; // The end of packets receiving, what if the ACK isn't successfully delivered?
            }
        }

        // Now Reconstruct and update GameState
        gameState = Packet.processGameStatePackets(map);
    }

    /**
     * Wait to get data from a datagram socket
     */
    public void waiting() throws IOException {
        System.out.println("Waiting...");
        while (true) {
            byte[] msg = new byte[1024];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            client_socket.receive(packet);

            Packet.Opcode packetOpcode = Packet.extractOpcode(msg);
            switch (packetOpcode) {
                case JOIN:
                    System.out.println("Join");
                    break;
                case START:
                    System.out.println("Start");
                    break;
                case UPDATE:
                    System.out.println("Send Game State");
                    break;
                case RECONNECT:
                    System.out.println("Reconnect");
                    break;
                case GAME_OVER:
                    System.out.println("Game Over");
                    break;
                case HEARTBEAT_REQUEST:
                    System.out.println("Heartbeat Request");
                    heartbeatReceived = false;
                    startRaftTimer();
                    break;
                case HEARTBEAT:
                    System.out.println("Heartbeat Obtained");
                    lastHeartbeatReceived = System.currentTimeMillis();
                    heartbeatReceived = true;
                    stopRaftTimer();
                    break;
                case VOTE_REQUEST:
                    // again inefficient, but i'm just trying to get this working - sl
                    ByteBuffer voteRequestBuffer = ByteBuffer.wrap(msg);
                    voteRequestBuffer.getShort(); // Skip opcode
                    short term = voteRequestBuffer.getShort();
                    int candidateId = voteRequestBuffer.getInt();

                    if (term > currentTerm) {
                        currentTerm = term;
                        vote = candidateId;
                        raftState = RaftState.FOLLOWER;
                    }

                    // only send to candidate btw...
                    if ((term == currentTerm) && (vote == -1 || vote == candidateId)) {
                        vote = candidateId;
                        System.out.println("Voting for candidate " + candidateId + " in term " + term);
                        byte[] voteGranted = ByteBuffer.allocate(2).putShort((short) Packet.Opcode.VOTE_GRANTED.ordinal()).array();
                        DatagramPacket response = new DatagramPacket(voteGranted, voteGranted.length, packet.getAddress(), packet.getPort());
                        client_socket.send(response);
                    } else {
                        System.out.println("Denying vote to candidate " + candidateId);
                        byte[] voteDenied = ByteBuffer.allocate(2).putShort((short) Packet.Opcode.VOTE_DENIED.ordinal()).array();
                        DatagramPacket response = new DatagramPacket(voteDenied, voteDenied.length, packet.getAddress(), packet.getPort());
                        client_socket.send(response);
                    }
                    break;
                case VOTE_GRANTED:
                    if (raftState == RaftState.CANDIDATE) {
                        int voterId = Packet.getVoterId(msg);
                        if (!votersThisTerm.contains(voterId)) {
                            votersThisTerm.add(voterId);
                            votesReceived++;
                            System.out.println("Received vote from " + voterId + " (" + votesReceived + " total)");

                            int majority = (gameState.getPlayers().size() / 2) + 1;
                            if (votesReceived >= majority) {
                                becomeLeader();
                                votersThisTerm.clear();
                            }
                        }
                    }
                    break;
                case VOTE_DENIED:
                    System.out.println("Vote Denied");
                    break;
            }
            System.out.println("Data Received.");
        }
    }


    /**
     * RAFT IMPLEMENTATION
     * <p>
     * Explanation of the voting process:
     * <ol>
     *     <li>Only one node becomes a candidate and starts the election.</li>
     *     <li>Every other node remains a follower, and will only vote once per term</li>
     *     <li>Followers vote for a candidate — they do not vote for themselves unless they become a candidate</li>
     * </ol>
     * If every candidate could vote for themselves, then it would reach a deadlock.
     * <p>
     * Step-by-Step Process:
     * <ol>
     *     <li>If a follower hasn't heard from a leader (heartbeat) within a timeout, it becomes a candidate.</li>
     *     <li>It increments its term, votes for itself, and sends RequestVote messages to all other nodes.</li>
     *     <li>The followers receive the vote request</li>
     *     <ol>
     *         <li>If the term of the received vote request is greater than or equal to its own term, it votes for that candidate. Then records it.</li>
     *         <li>Else if it's lower, it ignores the request.</li>
     *         <li>If it has already voted, it does not vote again.</li>
     *     </ol>
     *     <li> The candidate tallies its votes. If it's majority, it becomes leader.</li>
     *     <li> Else, waits for a timeout from another candidate, then restart the process.</li>
     * </ol>
     */
    // used to determine who is the leader
    short currentTerm = 1;
    // how long it should take before a new election is started
    int electionTimeoutMS = ThreadLocalRandom.current().nextInt(2000, 4000);
    // who the client voted for (during the election)
    int vote = -1;
    // used to see if we received the heartbeat;
    // volatile because timerTask creates a new thread that needs to know the status of this value.
    volatile boolean heartbeatReceived = true;
    // counting votes
    private int votesReceived = 0;
    // storing who voted
    private Set<Integer> votersThisTerm = new HashSet<>();


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
     * In RAFT Typical, you send a heartbeat every x ms.
     * In our modified RAFT, you request a heartbeat and receive it.
     */
    public void requestHeartbeat() {
        byte[] requestHeartbeat = Packet.createAckPacket(Packet.Opcode.HEARTBEAT, currentTerm);
        sendPacketToAllClients(requestHeartbeat);
    }

    /**
     * AppendEntries RPCs - these are known as heartbeats in the RAFT protocol.
     * It is used to let the followers know that the leader is still alive.
     *
     * @throws IOException - if an I/O error occurs (should never occur)
     */
    public void sendHeartbeats() {
        byte[] heartbeat = Packet.createAckPacket(Packet.Opcode.HEARTBEAT, currentTerm);
        sendPacketToAllClients(heartbeat);
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
    // could this code be a bit off?
    // is the timer ever allowed to run out? because we create a new timer task every time... look into this
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
                    requestHeartbeat();

                    // Check if enough time has passed without a heartbeat
                    // we need to implement updating this when we receive a heartbeat, which should be added to the main switch-case
                    while (!heartbeatReceived) {
                        if (currentTime - lastHeartbeatReceived > electionTimeoutMS) {
                            handleElectionTimeout();
                        }
                    }
//                    if (currentTime - lastHeartbeatReceived > electionTimeoutMS) {
//                        handleElectionTimeout();
//                    }
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
     * 4. Let everyone else know
     * 5.
     */
    public void holdRAFTElection() {
        raftState = RaftState.CANDIDATE;
        currentTerm++;
        vote = this.id;
        votesReceived = 1; // Vote for self
        votersThisTerm.clear();
        votersThisTerm.add(this.id);

        System.out.println("Node " + id + " starting election for term " + currentTerm);
        byte[] voteRequest = Packet.createVoteRequest(currentTerm, id, id); // voting for itself, hence id, id
        sendPacketToAllClients(voteRequest);
    }


    /**
     * Converts raftState to leader.
     * Sends a heartbeat which implicitly notifies every client that a new leader has been picked.
     */
    private void becomeLeader() {
        raftState = RaftState.LEADER;
        vote = -1;
        System.out.println("Node " + id + " became LEADER for term " + currentTerm);
        sendHeartbeats();
    }


    /**
     * Broadcast packet data to all clients, except for self.
     * Used for voting elections and can be used for updating game state.
     * Can be used by {@link #sendPacketToAllClients(byte[])} to send multiple packets (multiplexing)
     *
     * @param packet - the data you wish to send the client; the client will catch the type of request and act accordingly
     */
    private void sendPacketToAllClients(byte[] packet) {
        for (Player player : gameState.getPlayers().values()) {
            if (player.getID() == this.id) continue; // don't send to self
            try {
                DatagramPacket votePacket = new DatagramPacket(
                        packet, packet.length, player.getAddress(), player.getPort());
                client_socket.send(votePacket);
            } catch (IOException e) {
                System.err.println("Failed to send vote request to " + player.getID());
            }
        }
    }

    /**
     * Broadcast multiplexed packets to all clients, except for self.
     * Uses {@link #sendPacketToAllClients(byte[])}
     *
     * @param packets - a list of multiplexed packets to send
     */
    private void sendPacketToAllClients(List<byte[]> packets) {
        for (byte[] packet : packets) {
            sendPacketToAllClients(packet);
        }
    }
}
