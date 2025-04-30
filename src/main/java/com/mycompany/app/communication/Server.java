package com.mycompany.app.communication;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;

public class Server implements Runnable {
    public static int PORT = 26882;

    public static void runServer() {
        int maxBufferSize = 8;
        try (DatagramSocket serverSocket = new DatagramSocket(26882)) {
            System.out.println("Datagram Listening...");
            try {
                DatagramPacket packet = new DatagramPacket(new byte[maxBufferSize], maxBufferSize);
                System.out.println("waiting on receive");
                serverSocket.receive(packet);
                System.out.println("received");

                maxBufferSize = packet.getLength();
                byte[] data = packet.getData();
                System.out.println("Decoded byte array: " + data.length);
                DatagramPacket responsePacket = new DatagramPacket(
                        data, maxBufferSize, packet.getAddress(), packet.getPort());
                serverSocket.send(responsePacket);
            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.err.println("Error reading from client: " + e.getMessage());
            }
        } catch (SocketException e) {
            System.err.println("Socket failure");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        runServer();
    }
}
