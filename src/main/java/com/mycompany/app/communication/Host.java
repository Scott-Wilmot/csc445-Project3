package com.mycompany.app.communication;

import java.io.EOFException;
import java.io.IOException;
import java.net.*;

public class Host implements Runnable {
    public static int PORT = 8080;

    public static void runServer() {
        int maxBufferSize = 8;
        try (DatagramSocket serverSocket = new DatagramSocket(8080)) {
            System.out.println("Datagram Listening...");
            try {
                DatagramPacket packet = new DatagramPacket(new byte[maxBufferSize], maxBufferSize);
                serverSocket.receive(packet);

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

    public static void main(String[] args) {
        runServer();
    }
}
