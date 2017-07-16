package org.lotuscloud.wrapper.main;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.lennarth.jxutils.JsonLanguage;
import com.lennarth.jxutils.Language;
import org.lotuscloud.api.console.ConsoleReader;
import org.lotuscloud.api.crypt.Crypter;
import org.lotuscloud.api.logging.LogLevel;
import org.lotuscloud.api.logging.Logger;
import org.lotuscloud.api.network.PacketClient;
import org.lotuscloud.api.network.PacketServer;
import org.lotuscloud.api.packet.RegisterPacket;
import org.lotuscloud.api.packet.RegisteredPacket;
import org.lotuscloud.wrapper.handler.StartServerHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Scanner;

/**
 * Copyright (c) 2017 Lennart Heinrich
 * www.lennarth.com
 */
public class Wrapper {

    public static Wrapper instance;
    public Logger logger;
    public JsonObject config;
    public ConsoleReader console;
    public PacketServer server;
    public PacketClient client;
    public String masterHost;
    public int masterPort;
    public int bindPort;
    public Language language;

    public Wrapper() {
        instance = this;

        try {
            File file = new File("config.json");
            if (!file.exists())
                Files.write(Paths.get("config.json"), "{}".getBytes());
            config = Json.parse(new String(Files.readAllBytes(Paths.get("config.json")), StandardCharsets.UTF_8)).asObject().get("wrapper").asObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        loadLanguageFile();

        logger = new Logger(LogLevel.DEBUG);
        console = new ConsoleReader();

        masterHost = config.getString("master-host", "127.0.0.1");
        masterPort = config.getInt("master-port", 1241);

        bindPort = config.getInt("port", 1250);

        KeyPair keyPair = Crypter.generateKeyPair(2048);
        client = new PacketClient(keyPair);

        RegisteredPacket registeredPacket = (RegisteredPacket) client.registerWrapper(masterHost, masterPort, new RegisterPacket(bindPort, keyPair.getPublic()));

        if (registeredPacket.success)
            logger.log(language.get("wrapper_registered"), LogLevel.INFO);
        else
            logger.log(language.get("wrapper_register_fail").replace("$error", registeredPacket.error));

        server = new PacketServer(bindPort, client.key);
        server.bind();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.close()));

        server.registerHandler("startserver", new StartServerHandler());
        server.acceptIP(masterHost);

        logger.log(language.get("wrapper_started"), LogLevel.INFO);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("    __          __             ________                __\n   / /   ____  / /___  _______/ ____/ /___  __  ______/ /\n  / /   / __ \\/ __/ / / / ___/ /   / / __ \\/ / / / __  / \n / /___/ /_/ / /_/ /_/ (__  ) /___/ / /_/ / /_/ / /_/ /  \n/_____/\\____/\\__/\\__,_/____/\\____/_/\\____/\\__,_/\\__,_/");
        System.out.println("Wrapper - Copyright (c) 2017 Lennart Heinrich");
        System.out.println("Licensed under the Apache License, Version 2");

        if (!Files.exists(Paths.get("license-terms.txt")) || !new String(Files.readAllBytes(Paths.get("license-terms.txt"))).replace(" ", "").equalsIgnoreCase("accepted=true")) {
            System.out.print("Do you accept the license terms? [y/N]: ");

            if (new Scanner(System.in).nextLine().equalsIgnoreCase("y")) {
                try {
                    Files.write(Paths.get("license-terms.txt"), "accepted=true".getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Stopping Wrapper...");
                System.exit(0);
            }
        }

        System.out.println("Starting Wrapper...");

        new Wrapper();
    }

    private void loadLanguageFile() {
        String resourceName = "/language_" + config.getString("language", "en") + ".json";

        InputStream stream = null;
        OutputStream resStreamOut = null;

        String jarFolder;

        try {
            jarFolder = new File(Wrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');

            stream = Wrapper.class.getResourceAsStream(resourceName);

            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];

            resStreamOut = new FileOutputStream(jarFolder + resourceName);

            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }

            JsonLanguage jsonLanguage = new JsonLanguage(new String(Files.readAllBytes(new File(jarFolder, resourceName).toPath()), StandardCharsets.UTF_8));
            language = new Language(jsonLanguage);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                stream.close();
                resStreamOut.close();
            } catch (Exception ex) {
            }
        }
    }
}