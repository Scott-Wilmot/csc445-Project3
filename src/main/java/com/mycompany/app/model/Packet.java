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
    public short block_num;
    public byte[] data;

    /**
     * Used to reference what the type of data the client is attempting to send
     */
    public enum Opcode {
        JOIN,
        SEND_GAME_STATE,
        HEARTBEAT,
    }

    /**
     * Constructor for join request packets
     * @param opCode
     * @param id
     */
    private Packet(short opCode, short id) {
        this.opCode = opCode;
        this.id = id;
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
     * @param opCode
     * @param id
     * @return
     * @throws IOException
     */
    public static byte[] createJoinPacket(short opCode, short id) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(opCode);
        dos.writeShort(id);
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
        return new Packet(opCode, id);
    }

    /**
     * Splits a GameState object into multiple properly sized packets, ready for transmission.
     * GameState needs to be serialized then split into chunks
     * @param gs
     * @return
     */
    public static Packet[] createGameStatePackets(GameState gs) throws IOException {
        ArrayList<Packet> packets = new ArrayList<>();
        ByteBuffer ser_gs = ByteBuffer.wrap(serialize(gs));
        int data_len = 1020; // Magic number is packet size (1024) minus the opCode and BlockNum sizes

        short block_num = 0;
        while (true) {
            int remaining_bytes = ser_gs.remaining();

            if (remaining_bytes >= data_len) {
                byte[] data = new byte[data_len];
                ser_gs.get(data);
                packets.add(new Packet((short) 1, block_num, data));
                block_num++;
            } else { // Not enough data for full packet
                byte[] data = new byte[remaining_bytes];
                ser_gs.get(data);
                packets.add(new Packet((short) 1, block_num, data));
                break;
            }
        }

        return (Packet[]) packets.toArray();
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

    /**
     * Creates a acknowledgement packet used to verify server-client communication.
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
    public static byte[] createAckPacket(Opcode opcode, int specialNum) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // Write the 2-byte opcode
        output.write(new byte[]{0x00, (byte) opcode.ordinal()});

        // Write the 4-byte blockNum (big-endian order)
        output.write((byte) (specialNum >> 24));
        output.write((byte) (specialNum >> 16));
        output.write((byte) (specialNum >> 8));
        output.write((byte) specialNum);

        return output.toByteArray();
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
     *
     * @param packet - the byte array containing the acknowledgement packet
     * @return the extracted special number as an int
     */
    static int getBlockNumFromAckPacket(byte[] packet) {
        // Use bitwise shifts and the bitwise OR operator (|) to combine the bytes.
        // The & 0xFF is important to treat each byte as an unsigned value before shifting,
        // preventing sign extension issues with negative byte values in Java.
        int blockNum = ((packet[2] & 0xFF) << 24) |
                ((packet[3] & 0xFF) << 16) |
                ((packet[4] & 0xFF) << 8)  |
                (packet[5] & 0xFF);

        return blockNum;
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
     * @param opcode        - the request made by the client {JOIN, SEND_GAME_STATE, HEARTBEAT}
     * @param blockNum      - unique number used to identify packet data for ordering
     * @param state         - the {@link GameState} shared between servers-clients
     * @param encryptionKey - key used for encrypting packet communication
     * @return data byte[] packet used to send partitioned/whole Gamestate data.
     * @throws IOException - if an I/O error occurs (should never occur)
     */
    static byte[] sendGamePacket(Opcode opcode, int blockNum, int encryptionKey, GameState state) throws IOException {
        // Setup: Serializing state
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectSerializer = new ObjectOutputStream(output);
        objectSerializer.writeObject(state);
        objectSerializer.flush();
        byte[] stateBytes = output.toByteArray();
        output.reset();

        // Creating the packet
        output.write(new byte[]{0x00, (byte) opcode.ordinal()});
        output.write((byte) (blockNum >> 24));
        output.write((byte) (blockNum >> 16));
        output.write((byte) (blockNum >> 8));
        output.write((byte) blockNum);
        output.write(stateBytes);

        return output.toByteArray();
    }


    public static void main(String[] args) throws IOException {
        GameState gameState = new GameState();
        byte[] data = sendGamePacket(Opcode.JOIN, 01, 01, gameState);
        gameState.addPlayer(1, new Player(InetAddress.getByName("localhost"), 9090));
        gameState.addPlayer(2, new Player(InetAddress.getByName("localhost"), 9090));
        gameState.addPlayer(3, new Player(InetAddress.getByName("localhost"), 9090));
        System.out.printf("Length: %d\n", data.length);
    }
}
