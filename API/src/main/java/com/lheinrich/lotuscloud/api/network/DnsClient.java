package com.lheinrich.lotuscloud.api.network;

import com.lheinrich.lotuscloud.api.packet.DnsPacket;
import com.lheinrich.lotuscloud.api.packet.ErrorPacket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class DnsClient {

    public static Packet update(String host, int port, String ip, int dnsPort) {
        try {
            Socket socket = new Socket(host, port);

            if (socket.isClosed()) {
                return new ErrorPacket("socket closed");
            }

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new DnsPacket(ip, dnsPort));
            out.flush();

            Packet response = (Packet) in.readObject();

            in.close();
            out.close();
            socket.close();

            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ErrorPacket(ex.getMessage());
        }
    }
}
