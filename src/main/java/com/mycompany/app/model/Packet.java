package com.mycompany.app.model;

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * A utility class for creating establishing packet based communication.
 * Holds multiple parameters associated with a packet for easy access to packet data
 * <p>
 * This class includes methods for creation of data packets, ack packets, data extraction,
 * and other custom packets.
 */
public class Packet {

    public short opCode;
    public short id;
    public int port;
    public short block_num;
    public byte[] data;

    public static final int PACKET_SIZE = 1024;
    public static final int OPCODE_SIZE = Short.SIZE / 8;
    public static final int TERM_SIZE = Short.SIZE / 8;
    public static final int BLOCKNUM_SIZE = Short.SIZE / 8;
    public static final int ENCRYPTION_SIZE = 16;
    public static final int DATA_SIZE = PACKET_SIZE - OPCODE_SIZE - BLOCKNUM_SIZE; // Add encryption size later

    /**
     * Used to reference what the type of data the client is attempting to send
     */
    public enum Opcode {
        JOIN,
        START,
        UPDATE,
        HEARTBEAT_REQUEST,
        HEARTBEAT,
        RECONNECT,
        GAME_OVER,
        VOTE_REQUEST,
        VOTE_GRANTED,
        VOTE_DENIED
    }

    /**
     * Constructor for join request packets
     * @param opCode
     * @param id
     */
    private Packet(short opCode, short id, int port) {
        this.opCode = opCode;
        this.id = id;
        this.port = port;
    }

    /**
     * Constructor for GameState packets
     * @param opCode
     */
    private Packet(short opCode, short block_num, byte[] data) {
        this.opCode = opCode;
        this.block_num = block_num;
        this.data = data;
    }

    /**
     * Serializes a GameState into a byte array and returns the byte array
     * @param gs
     * @return
     * @throws IOException
     */
    public static byte[] serialize(GameState gs) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(gs);
            return bos.toByteArray();
        }
    }

    /**
     * Creates a GameState object from a byte array
     * @param ser_gs
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static GameState deserialize(byte[] ser_gs) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(ser_gs);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (GameState) ois.readObject();
        }
    }

    /**
     * Takes in an OpCode and ID as shorts (2 Bytes) to place into a byte array.
     * Also adds a heartbeatPort for the client to connect to. port represented as an int
     * Packet has a total size of 8 Bytes
     * @param opCode
     * @param id
     * @return
     * @throws IOException
     */
    public static byte[] createJoinPacket(short opCode, short id, int heartbeatPort) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(opCode);
        dos.writeShort(id);
        dos.writeInt(heartbeatPort);
        return bos.toByteArray();
    }

    /**
     * Takes in a byte array and gets important information from the packet to return in a Packet object
     * @param bytes
     * @return
     */
    public static Packet processJoinPacket(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        short opCode = buf.getShort();
        short id = buf.getShort();
        int port = buf.getInt();
        return new Packet(opCode, id, port);
    }

    /**
     * Splits a GameState object into multiple properly sized packets, ready for transmission.
     * GameState needs to be serialized then split into chunks
     * @param gs
     * @return
     */
    public static ArrayList<Packet> createGameStatePackets(GameState gs) throws IOException {
        ArrayList<Packet> packets = new ArrayList<>();
        ByteBuffer ser_gs = ByteBuffer.wrap(serialize(gs));
        byte[] data;

        short block_num = 0;
        while (true) {
            int remaining_bytes = ser_gs.remaining();

            if (remaining_bytes >= DATA_SIZE) {
                data = new byte[DATA_SIZE];
                ser_gs.get(data, 0, DATA_SIZE);
                packets.add(new Packet((short) 1, block_num, data));
                block_num++;
            } else { // Not enough data for full packet
                data = new byte[remaining_bytes];
                ser_gs.get(data);
                packets.add(new Packet((short) 1, block_num, data));
                break;
            }

        }

        return packets;
    }

    public static GameState processGameStatePackets(HashMap<Short, byte[]> map) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        Set<Short> set = map.keySet();
        List<Short> keys = new ArrayList<>(set);
        Collections.sort(keys);

        // Place, in order, the data into a BOS
        for (Short key : keys) {
            byte[] data = map.get(key);
            dos.write(data);
        }

        // Now deserialize the data
        GameState deser_gs = Packet.deserialize(bos.toByteArray());
        return deser_gs;
    }

    public byte[] toGameStatePacket() {
        // Fill the buffer
        ByteBuffer buf = ByteBuffer.allocate(PACKET_SIZE);
        buf.putShort(opCode);
        buf.putShort(block_num);
        buf.put(data);

        // Process before returning
        buf.flip();
        byte[] data = new byte[buf.remaining()];
        buf.get(data);
        return data;
    }

    /**
     * Creates an acknowledgement packet used to verify server-client communication.
     * It can be used to join the game and verify if the client joined the game.
     * It is also used as a heartbeat packet for RAFT Protocol.
     * +------------+-----------+
     * |  OP-CODE   | BLOCK-NUM |
     * +------------+-----------+
     * | [2 bytes]  | [4 bytes] |
     * +------------+-----------+
     * <p/>
     *
     * @param opcode   - the request {Heartbeat, Ack}
     * @param specialNum - unique number sent through this packet
     * @return a byte[] packet with the opcode and blockNum
     * @throws IOException - if an I/O error occurs (should never occur)
     */
    public static byte[] createAckPacket(Opcode opcode, int specialNum) {
        ByteBuffer buffer = ByteBuffer.allocate(OPCODE_SIZE + BLOCKNUM_SIZE);
        buffer.putShort((short) opcode.ordinal());
        buffer.putInt(specialNum);
        return buffer.array();
    }


    /**
     * Creates a vote request packet for initiating a RAFT election.
     * This packet is sent by a candidate to all other nodes to request their vote.
     * <pre>
     * +------------+-----------+--------------+--------------+
     * |  OP-CODE   |   TERM    | CANDIDATE ID | VOTER ID     |
     * +------------+-----------+--------------+--------------+
     * | [2 bytes]  | [2 bytes] |  [4 bytes]   |  [4 bytes]   |
     * +------------+-----------+--------------+--------------+
     * </pre>
     *
     * @param term - the current term of the candidate requesting the vote
     * @param candidateId - the unique ID of the candidate requesting the vote
     * @return a byte array representing the vote request packet
     */
    public static byte[] createVoteRequest(short term, int candidateId, int voterId) {
        ByteBuffer buffer = ByteBuffer.allocate(OPCODE_SIZE + TERM_SIZE + BLOCKNUM_SIZE);
        buffer.putShort((short) Opcode.VOTE_REQUEST.ordinal());
        buffer.putShort(term);
        buffer.putInt(candidateId);
        buffer.putInt(voterId);
        return buffer.array();
    }

    // not optimal but it works.
    public static int getVoterId(byte[] packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        // discards unnecessary
        buffer.getShort();
        buffer.getShort();
        buffer.getInt();
        // send buffer
        return buffer.getInt();
    }



    /**
     * Extracts the special number from an acknowledgement packet.
     * Assumes the packet format is:
     * +------------+-----------+
     * |  OP-CODE   | BLOCK-NUM |
     * +------------+-----------+
     * | [2 bytes]  | [4 bytes] |
     * +------------+-----------+
     * The block number is expected to be a 4-byte integer in big-endian order starting at offset 2.
     * By doing buffer.getShort(), you discard the op-code and are able to retrieve the block number.
     *
     * @param packet - the byte array containing the acknowledgement packet
     * @return the extracted special number as an int
     */
    public static int getBlockNumFromAckPacket(byte[] packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        buffer.getShort();
        return buffer.getInt();
    }

    /**
     * Extracts Opcode from packet.
     * This is used by server and clients to what request the other device is making.
     * @param packet - any packet
     * @return Opcode value converted from its ordinal value
     */
    public static Opcode extractOpcode(byte[] packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        short opcodeOrdinal = buffer.getShort();
        return Opcode.values()[opcodeOrdinal];
    }


    /**
     * Creates a byte[] that contains: op-code, block-number, and data.
     * +------------+-----------+-------------+------------+
     * |  OP-CODE   | BLOCK-NUM | ENCRYPTION  |    DATA    |
     * +------------+-----------+-------------+------------+
     * | [2 bytes]  | [4 bytes] | [16 bytes]  | [Variable] |
     * +------------+-----------+-------------+------------+
     * DATA_SIZE is equal to: PACKET_SIZE - OP_CODE_SIZE - BLOCK_NUM_SIZE - ENCRYPTION_SIZE;
     * Or simply, DATA_SIZE = PACKET_SIZE - 22;
     * <p/>
     *
     * @param opcode        - the request made by the client {JOIN, UPDATE, HEARTBEAT}
     * @param blockNum      - unique number used to identify packet data for ordering
     * @param state         - the {@link GameState} shared between servers-clients
     * @param encryptionKey - key used for encrypting packet communication
     * @return data byte[] packet used to send partitioned/whole Gamestate data.
     * @throws IOException - if an I/O error occurs (should never occur)
     */
    public static byte[] createGamePacket(Opcode opcode, int blockNum, int encryptionKey, GameState state) throws IOException {
        // Serialize GameState
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(state);
        oos.flush();
        byte[] stateBytes = baos.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);
        buffer.putShort((short) opcode.ordinal());
        buffer.putInt(blockNum);
        buffer.put(new byte[ENCRYPTION_SIZE]);         // add encryption key in the future; for now, placeholder.
        buffer.put(stateBytes);

        return buffer.array();
    }

}
