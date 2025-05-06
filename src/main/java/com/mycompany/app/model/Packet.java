package com.mycompany.app.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * OPCODES:
 * <ol>
 *     <li>1: Join</li>
 *     <li></li>
 * </ol>
 */
public class Packet {
    enum Opcode {
        JOIN,
        SEND_GAME_STATE,
    }

    static void createAckPacket() {

    }

    /**
     * Creates a byte[] that contains: op-code, block-number, and data.
     * +-----------+-----------+---------------------+
     * |  OP-CODE  | BLOCK-NUM |      IMAGE-DATA     |
     * +-----------+-----------+---------------------+
     * | [2 BYTES] | [2 BYTES] | [AT MOST 508 BYTES] |
     * +-----------+-----------+---------------------+
     * <p>
     * @param opcode - the request made by the client {JOIN, SEND_GAME_STATE}
     * @param encryptionKey - key used for encrypting packet communication
     * @param blockNum - unique number used to identify packet data
     * @param state - the {@link GameState} shared between servers-clients
     * @return data byte[] packet used to send partitioned/whole Gamestate data.
     * @throws IOException - if an I/O error occurs (unlikely)
     */
    static byte[] sendGamePacket(Opcode opcode, int blockNum, GameState state, int encryptionKey) throws IOException {
        // Setup: Serializing state
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectSerializer = new ObjectOutputStream(output);
        objectSerializer.writeObject(state);
        objectSerializer.flush();
        byte[] stateBytes = output.toByteArray();
        output.reset();

        output.write(new byte[]{0x00, (byte) opcode.ordinal()});
        output.write((byte) (blockNum >> 8));
        output.write((byte) (blockNum & 0xFF));
        output.write(stateBytes);

        return output.toByteArray();
    }

    /**
     * The packet used to join the session.
     * OPCODE + INET-ADDRESS + (ENCRYPTION SCHEME)
     */
    static void syncPacket() {
        byte[] packet = new byte[4];
    }

    public static void main(String[] args) throws IOException {
        GameState gameState = new GameState();
//        byte[] data = sendGamePacket(Opcode.JOIN, gameState);
        gameState.addPlayer(1, new Player( InetAddress.getByName("localhost"), 9090));
        gameState.addPlayer(2, new Player( InetAddress.getByName("localhost"), 9090));
        gameState.addPlayer(3, new Player( InetAddress.getByName("localhost"), 9090));
//        System.out.printf("Length: %d\n", data.length);
    }
}
