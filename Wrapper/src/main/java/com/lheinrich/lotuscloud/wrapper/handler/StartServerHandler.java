package com.lheinrich.lotuscloud.wrapper.handler;

import com.lheinrich.lotuscloud.api.logging.LogLevel;
import com.lheinrich.lotuscloud.api.network.Handler;
import com.lheinrich.lotuscloud.api.network.Packet;
import com.lheinrich.lotuscloud.api.packet.ErrorPacket;
import com.lheinrich.lotuscloud.api.packet.OkPacket;
import com.lheinrich.lotuscloud.api.packet.StartServerPacket;
import com.lheinrich.lotuscloud.wrapper.main.Wrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class StartServerHandler extends Handler {

    @Override
    public Packet handle(Packet packet, String client) {
        StartServerPacket startServerPacket = (StartServerPacket) packet;
        File templateDir = new File("templates/" + startServerPacket.getTemplate());

        if (!templateDir.exists()) {
            return new ErrorPacket(Wrapper.instance.language.get("template_not_found"));
        }

        File tempDir = new File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        File dir;
        for (int i = 1; true; i++) {
            if ((dir = new File("temp/" + startServerPacket.getTemplate() + i)).exists()) {
                continue;
            }
            Wrapper.instance.logger.log(Wrapper.instance.language.get("copy_from_to").replace("$source", templateDir.getPath()).replace("$destination", dir.getPath()), LogLevel.DEBUG);
            break;
        }

        try {
            Files.copy(templateDir.toPath(), dir.toPath());
            for (File file : templateDir.listFiles()) {
                Files.copy(file.toPath(), Paths.get(dir.getPath() + "/" + file.getName()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ErrorPacket(ex.getMessage());
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(dir);
        processBuilder.command("java", "-jar", startServerPacket.getJarName());
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ErrorPacket(ex.getMessage());
        }
        new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            while (process.isAlive()) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        System.out.println(reader.readLine());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        return OkPacket.init;
    }
}
