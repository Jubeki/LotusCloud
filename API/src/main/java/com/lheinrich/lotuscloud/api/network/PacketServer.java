package com.lheinrich.lotuscloud.api.network;

import com.lheinrich.lotuscloud.api.crypt.Crypter;
import com.lheinrich.lotuscloud.api.packet.ErrorPacket;
import com.lheinrich.lotuscloud.api.packet.RegisterPacket;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class PacketServer {

    public SecretKey key;
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private HashMap<String, Handler> handlers = new HashMap<>();
    private ArrayList<String> acceptIP = new ArrayList<>();

    public PacketServer(int port) {
        this.port = port;
        key = Crypter.generateKey(128);
    }

    public PacketServer(int port, SecretKey key) {
        this.port = port;
        this.key = key;
    }

    public void bind() {
        close();
        try {
            serverSocket = new ServerSocket(port);

            Thread thread = new Thread(() -> {
                while (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();

                        executor.submit(() -> {
                            try {
                                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                                Object object = in.readObject();
                                boolean isRegisterPacket = object instanceof RegisterPacket;

                                String client = socket.getInetAddress().getHostAddress();
                                if (!acceptIP.contains(client) && !isRegisterPacket) {
                                    socket.close();
                                    System.out.println("unallowed request from " + client + " closed");
                                    return;
                                }
                                Packet packet;

                                if (isRegisterPacket)
                                    packet = (Packet) object;
                                else {
                                    byte[] decrypted = Crypter.decrypt(key, (byte[]) object, "AES");
                                    packet = (Packet) Crypter.toObject(decrypted);
                                }

                                if (handlers.containsKey(packet.getPacketName())) {
                                    Packet response = handlers.get(packet.getPacketName()).handle(packet, client);

                                    if (isRegisterPacket)
                                        out.writeObject(response);
                                    else
                                        out.writeObject(Crypter.encrypt(key, Crypter.toByteArray(response), "AES"));
                                } else
                                    out.writeObject(Crypter.encrypt(key, Crypter.toByteArray(new ErrorPacket("handler not found")), "AES"));

                                out.flush();
                                in.close();
                                out.close();

                                socket.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                if (!socket.isClosed())
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        });
                    } catch (IOException ex) {
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            serverSocket = null;
        }
    }

    public void registerHandler(String packetName, Handler handler) {
        unregisterHandler(packetName);
        handlers.put(packetName, handler);
    }

    public void unregisterHandler(String packetName) {
        if (handlers.containsKey(packetName))
            handlers.remove(packetName);
    }

    public void acceptIP(String ip) {
        if (!acceptIP.contains(ip))
            acceptIP.add(ip);
    }

    public void removeIP(String ip) {
        if (acceptIP.contains(ip))
            acceptIP.remove(ip);
    }

    public boolean isAcceptedIP(String ip) {
        return acceptIP.contains(ip);
    }

    public int getPort() {
        return port;
    }
}
