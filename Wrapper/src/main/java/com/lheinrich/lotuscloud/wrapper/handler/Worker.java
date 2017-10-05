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
package com.lheinrich.lotuscloud.wrapper.handler;

import com.lheinrich.lotuscloud.api.server.GameServer;
import com.lheinrich.lotuscloud.api.server.ProxyServer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Worker {

    private File templates;
    private File temporary;
    private Map<String, GameServer> gameServers = new HashMap<>();

    public Worker(File templates, File temporary) {
        this.templates = templates;
        this.temporary = temporary;
        new Thread(() -> {
            taskProcessor();
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (String server : gameServers.keySet()) {
                stopGame(server);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
            processTask();
        }));
    }

    public void taskProcessor() {
        while (true) {
            processTask();
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void processTask() {
        for (GameServer gameServer : gameServers.values()) {
            try {
                if (!gameServer.isOnline()) {
                    String name = gameServer.getName();
                    File dir = new File(temporary, name);
                    if (dir.exists()) {
                        deleteFolder(dir);
                    }
                    gameServers.remove(name);
                    System.out.println("Removed unused GameServer: " + name);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String startGame(String id, int port, int max) {
        File dir;
        for (int i = 1; true; i++) {
            if ((dir = new File(temporary, id + i)).exists()) {
                continue;
            }
            break;
        }
        copy(new File(templates, id), dir);

        File properties = new File(dir, "server.properties");
        if (!properties.exists()) {
            try {
                Files.write(properties.toPath(), ("online-mode=false\nserver-port=" + port + "\nmax-players=" + max + "\nserver-ip=127.0.0.1").getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                String lines = new String(Files.readAllBytes(properties.toPath()), StandardCharsets.UTF_8);
                String[] splitLines = lines.split("\n");

                for (String line : splitLines) {
                    switch (line.split("=")[0]) {
                        case "max-players":
                            lines = lines.replace(line, "max-players=" + max);
                            break;
                        case "server-port":
                            lines = lines.replace(line, "server-port=" + port);
                            break;
                        case "online-mode":
                            lines = lines.replace(line, "online-mod=false");
                            break;
                        case "server-ip":
                            lines = lines.replace(line, "server-ip=127.0.0.1");
                            break;
                    }
                }

                Files.write(properties.toPath(), lines.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(dir);
        processBuilder.command("java", "-jar", "server.jar");
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        gameServers.put(dir.getName(), new GameServer(dir.getName(), process, port));
        return dir.getName();
    }

    public void stopGame(String name) {
        GameServer server = gameServers.get(name);
        server.getProcess().destroy();
    }

    @Deprecated
    public ProxyServer startProxy() {
        return null;
    }

    private void copy(File dir, File to) {
        try {
            Files.copy(dir.toPath(), to.toPath());
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    copy(file, new File(to, file.getName()));
                } else {
                    Files.copy(file.toPath(), Paths.get(to.getPath() + "/" + file.getName()));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public GameServer getGameServer(String name) {
        return gameServers.get(name);
    }

    public boolean existsGameServer(String name) {
        return gameServers.containsKey(name);
    }
}
