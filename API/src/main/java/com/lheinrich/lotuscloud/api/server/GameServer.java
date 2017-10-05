/*
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lheinrich.lotuscloud.api.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class GameServer {

    private String name;
    private int port;
    private String host;
    private Process process;

    @Deprecated
    public GameServer(String name, Process process, int port, String host) {
        this.host = host;
        this.name = name;
        this.port = port;
        this.process = process;
    }

    public GameServer(String name, Process process, int port) {
        this.host = "127.0.0.1";
        this.name = name;
        this.port = port;
        this.process = process;
    }

    public boolean isOnline() {
        return !connect()[0].equals("error: not online");
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMotd() {
        return connect()[0];
    }

    public int getOnline() {
        return Integer.valueOf(connect()[1]);
    }

    public int getMax() {
        return Integer.valueOf(connect()[2]);
    }

    public Process getProcess() {
        return process;
    }

    private String[] connect() {
        try {
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(15 * 1000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.write(0xFE);

            int b;
            StringBuilder str = new StringBuilder();
            while ((b = in.read()) != -1) {
                if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                    str.append((char) b);
                }
            }

            String[] data = str.toString().split("§");
            return data;
        } catch (IOException ex) {
            if (!ex.getMessage().toLowerCase().contains("connection refused")) {
                ex.printStackTrace();
            }
        }

        String[] notOnline = {"error: not online", "0", "0"};
        return notOnline;
    }

    public static boolean isServerOn(String host, int port) {
        return new GameServer(null, null, port, host).isOnline();
    }
}
