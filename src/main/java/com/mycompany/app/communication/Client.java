package com.mycompany.app.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client {

    public static void main(String[] args) {
        try {
            String myIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println(myIP);
            create(myIP);
        } catch (Exception e) {

        }
    }

    public static DatagramSocket create(String ip) {
        try {
            byte[] buf = "10101010".getBytes();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("129.3.125.7"), 8080);
            socket.send(packet);
            System.out.println("Hi Saurav");
            socket.receive(packet);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
