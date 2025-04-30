package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Server {
    public static int PORT = 8080;

    public static void main(String[] args) {
        int maxBufferSize = 8;
        try (DatagramSocket socket = new DatagramSocket()) {
            System.out.println("Datagram Listening...");

            DatagramPacket packet = new DatagramPacket(new byte[maxBufferSize], maxBufferSize);
            socket.receive(packet);

            System.out.println("Packet Received: " + new String(packet.getData()));

            DatagramPacket responsePacket = new DatagramPacket(
                    packet.getData(), maxBufferSize, packet.getAddress(), packet.getPort());

            socket.send(responsePacket);
        } catch (SocketException e) {
            System.err.println("Socket Error");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Socket received corrupted packet");
            throw new RuntimeException(e);
        }
    }
}
