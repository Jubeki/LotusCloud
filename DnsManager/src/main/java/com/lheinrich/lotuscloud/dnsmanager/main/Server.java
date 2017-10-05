package com.lheinrich.lotuscloud.dnsmanager.main;

import com.lheinrich.lotuscloud.api.network.Packet;
import com.lheinrich.lotuscloud.api.packet.DnsPacket;
import com.lheinrich.lotuscloud.api.packet.OkPacket;
import com.lheinrich.lotuscloud.api.packet.RegisterPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Server {

    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public Server(int port) {
        this.port = port;
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

                                DnsPacket packet = (DnsPacket) object;

                                CloudFlareTools.updateDns(client, packet.getIp(), packet.getPort());

                                Packet response = new OkPacket("updated");

                                out.writeObject(response);

                                out.flush();
                                in.close();
                                out.close();

                                socket.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                if (!socket.isClosed()) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
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

    public int getPort() {
        return port;
    }
}
