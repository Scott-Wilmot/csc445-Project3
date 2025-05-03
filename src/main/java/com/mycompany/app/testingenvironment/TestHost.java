package com.mycompany.app.testingenvironment;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import static com.mycompany.app.testingenvironment.Config.BUFFER_SIZE;

public class TestHost {
    private DatagramSocket socket;
    private final ByteBuffer buffer;

    public TestHost() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    void run() {
        try {
            socket = new DatagramSocket(12345);

            for (;;) {
                DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit());
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                System.out.println("Received Data");

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("from: " + address.getHostAddress() + ":" + port + ": " + message);
                packet = new DatagramPacket(buffer.array(), buffer.limit(), address, port);
                socket.send(packet);

//                buffer.flip();
                buffer.clear();
            }

        } catch (SocketException e) {
            System.err.println("DatagramSocket Corrupted");
            throw new RuntimeException(e);

        } catch (IOException e) {
            System.err.println("Received Packet Corrupted");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        TestHost test = new TestHost();
        test.run();
    }
}
