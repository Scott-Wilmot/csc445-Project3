package com.mycompany.app.communication;

import java.io.IOException;
import java.net.*;

public class Client implements Runnable {
    public static int PORT = 26881;
    public static int SERVER_PORT = 26882;

    public static void runClient() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            InetAddress address = InetAddress.getByName("localhost");
            String testMessage = "onetwofr";
            System.out.println(testMessage.getBytes().length);
            DatagramPacket packet = new DatagramPacket(testMessage.getBytes(), testMessage.length(), address, SERVER_PORT);
            socket.send(packet);
            System.out.println("sent");

            // Receive the response from the server
            byte[] buffer = new byte[testMessage.length()];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);
            byte[] data = receivePacket.getData();

        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        runClient();
    }
}
