package com.mycompany.app.communication;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Client {

    DatagramSocket client_socket;
    int id; // id should have ranges of 0-3?

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.connect("129.3.125.8", 57278);
    }

    Client() throws SocketException {
        client_socket = new DatagramSocket(0);
    }

    /**
     * Connects to the host of a game and receives a unique id from the host upon successful connection
     *
     * @param ip
     * @param port
     * @throws SocketException
     */
    public void connect(String ip, int port) throws IOException {
        byte[] buf = "".getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);

        client_socket.send(packet);

        client_socket.receive(packet);
        id = ByteBuffer.wrap(packet.getData()).getInt();
    }

    public void send_update() {

    }

}
