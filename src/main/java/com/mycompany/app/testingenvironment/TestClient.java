package com.mycompany.app.testingenvironment;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

import static com.mycompany.app.testingenvironment.Config.BUFFER_SIZE;

public class TestClient {
    private DatagramSocket socket;
    private InetAddress ip;
    private ByteBuffer buffer;

    public TestClient(InetAddress ip) {
        this.ip = ip;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    void run() {
        try {
            socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);

            for (;;) {
                System.out.println("Enter a message to send to the client.");
                String message = scanner.nextLine();
                buffer.put(message.getBytes());
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), ip, 12345);
                socket.send(packet);

                socket.receive(packet);
                String serverMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("The server says: " + serverMessage);

            }

        } catch (SocketException e) {
            System.err.println("DatagramSocket Corrupted");
            throw new RuntimeException(e);

        } catch (IOException e) {
            System.err.println("Received Packet Corrupted");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("localhost");
        TestClient testClient = new TestClient(ip);
        testClient.run();
    }

}
