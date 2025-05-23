package com.mycompany.app.communication;

import com.mycompany.app.model.GameState;
import com.mycompany.app.model.Packet;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {

        byte[] bytes = Packet.createJoinPacket((short) 233, (short) 2);

        Packet p = Packet.processJoinPacket(bytes);
        System.out.println("OpCode: " + p.opCode + ", ID: " + p.id);

    }
}
