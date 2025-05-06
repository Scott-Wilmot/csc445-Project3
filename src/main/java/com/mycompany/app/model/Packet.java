package com.mycompany.app.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

/**
 * A utility class for creating establishing packet based communication.
 * <p>
 * This class includes methods for creation of data packets, ack packets, data extraction,
 * and other custom packets.
 */
public class Packet {
    /**
     * Used to reference what the type of data the client is attempting to send
     */
    enum Opcode {
        JOIN,
        SEND_GAME_STATE,
    }

    /**
     * Creates an acknowledgement packet used to verify server-client communication.
     * It can be used to join the game and verify if the client joined the game.
     * +------------+-----------+
     * |  OP-CODE   | BLOCK-NUM |
     * +------------+-----------+
     * | [2 bytes]  | [2 bytes] |
     * +------------+-----------+
     * <p/>
     *
     * @param opcode   - the request
     * @param blockNum - unique number identifying when this packet was sent in relation to other packets
     * @return a byte[] packet with the opcode and blockNu
     * @throws IOException - if an I/O error occurs (should never occur)
     */
    static byte[] createAckPacket(Opcode opcode, int blockNum) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(new byte[]{0x00, (byte) opcode.ordinal()});
        output.write((byte) (blockNum >> 8));
        output.write((byte) (blockNum & 0xFF));
        return output.toByteArray();
    }

    /**
     * Creates a byte[] that contains: op-code, block-number, and data.
     * +------------+-----------+-------------+------------+
     * |  OP-CODE   | BLOCK-NUM | ENCRYPTION  |    DATA    |
     * +------------+-----------+-------------+------------+
     * | [2 bytes]  | [2 bytes] | [16 bytes]  | [Variable] |
     * +------------+-----------+-------------+------------+
     * DATA_SIZE is equal to: PACKET_SIZE - OP_CODE_SIZE - BLOCK_NUM_SIZE - ENCRYPTION_SIZE;
     * Or simply, DATA_SIZE = PACKET_SIZE - 20;
     * <p/>
     *
     * @param opcode        - the request made by the client {JOIN, SEND_GAME_STATE}
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
        output.write((byte) (blockNum >> 8));
        output.write((byte) (blockNum & 0xFF));
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
