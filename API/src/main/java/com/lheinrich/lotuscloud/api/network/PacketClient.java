package com.lheinrich.lotuscloud.api.network;

import com.lheinrich.lotuscloud.api.crypt.Crypter;
import com.lheinrich.lotuscloud.api.packet.ErrorPacket;
import com.lheinrich.lotuscloud.api.packet.RegisteredPacket;

import javax.crypto.SecretKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class PacketClient {

    public KeyPair keyPair;
    public SecretKey key;

    public PacketClient(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public Packet request(String host, int port, Packet packet) {
        try {
            Socket socket = new Socket(host, port);

            if (socket.isClosed())
                return new ErrorPacket("socket closed");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(Crypter.encrypt(key, Crypter.toByteArray(packet), "AES"));
            out.flush();

            Packet response = (Packet) Crypter.toObject(Crypter.decrypt(key, (byte[]) in.readObject(), "AES"));

            in.close();
            out.close();
            socket.close();

            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ErrorPacket(ex.getMessage());
        }
    }

    public Packet registerWrapper(String host, int port, Packet packet) {
        try {
            Socket socket = new Socket(host, port);

            if (socket.isClosed())
                return new ErrorPacket("socket closed");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(packet);
            out.flush();

            Packet response = (Packet) in.readObject();
            RegisteredPacket registeredPacket = (RegisteredPacket) response;

            key = (SecretKey) Crypter.toObject(Crypter.decrypt(keyPair.getPrivate(), (byte[]) registeredPacket.key, "RSA"));

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