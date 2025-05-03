package com.mycompany.app.model;

/**
 * OPCODES:
 * <ol>
 *     <li>1: Join</li>
 *     <li></li>
 * </ol>
 */
public class Packet {
    enum opcodes {
        JOIN,
    }

    static void createAckPacket() {

    }

    /**
     * The action chosen by the User.
     */
    static void createActionPacket() {

    }

    /**
     * The packet used to join the session
     */
    static void syncPacket() {
        byte[] packet = new byte[4];


    }
}
