package com.lheinrich.lotuscloud.master.main;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.lheinrich.jxutils.JsonLanguage;
import com.lheinrich.jxutils.Language;
import com.lheinrich.lotuscloud.api.console.ConsoleCommand;
import com.lheinrich.lotuscloud.api.console.ConsoleReader;
import com.lheinrich.lotuscloud.api.crypt.Crypter;
import com.lheinrich.lotuscloud.api.database.DatabaseManager;
import com.lheinrich.lotuscloud.api.logging.LogLevel;
import com.lheinrich.lotuscloud.api.logging.Logger;
import com.lheinrich.lotuscloud.api.network.DnsClient;
import com.lheinrich.lotuscloud.api.network.Handler;
import com.lheinrich.lotuscloud.api.network.Packet;
import com.lheinrich.lotuscloud.api.network.PacketClient;
import com.lheinrich.lotuscloud.api.network.PacketServer;
import com.lheinrich.lotuscloud.api.packet.GameServerPacket;
import com.lheinrich.lotuscloud.api.packet.RegisterPacket;
import com.lheinrich.lotuscloud.api.packet.RegisteredPacket;
import com.lheinrich.lotuscloud.api.packet.StartGameServerPacket;
import com.lheinrich.lotuscloud.master.web.WebServer;
import com.lheinrich.lotuscloud.master.webhandler.Dashboard;
import com.lheinrich.lotuscloud.master.webhandler.Groups;
import com.lheinrich.lotuscloud.master.webhandler.Logout;
import com.lheinrich.lotuscloud.master.webhandler.MainWebHandler;
import com.lheinrich.lotuscloud.master.webhandler.Style;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Master {

    public static Master instance;
    public Logger logger;
    public DatabaseManager databaseManager;
    public JsonObject config;
    public ConsoleReader console;
    public PacketServer server;
    public PacketClient client;
    public HashMap<String, Integer> wrapper = new HashMap<>();
    public WebServer webServer;
    public Language language;

    public Master() {
        instance = this;

        try {
            File file = new File("config.json");
            if (!file.exists()) {
                Files.write(Paths.get("config.json"), "{}".getBytes());
            }
            config = Json.parse(new String(Files.readAllBytes(Paths.get("config.json")), StandardCharsets.UTF_8)).asObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        loadLanguageFile();

        logger = new Logger(LogLevel.DEBUG);

        JsonObject mongoConfig = config.get("mongodb").asObject();

        logger.log(language.get("connecting_to_db"), LogLevel.INFO);
        databaseManager = new DatabaseManager(mongoConfig.getString("host", "127.0.0.1"), mongoConfig.getInt("port", 27017), mongoConfig.getString("database", ""), mongoConfig.getString("user", ""), mongoConfig.getString("password", ""));
        logger.log(language.get("connected_to_db"), LogLevel.INFO);

        console = new ConsoleReader();

        server = new PacketServer(1241);
        server.bind();

        webServer = new WebServer(1735);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
            webServer.close();
        }));

        registerHandler();

        client = new PacketClient(null);
        client.key = server.key;

        DnsClient.update("127.0.0.1", 2038, "84.200.206.225", 25565);

        console.register("start", new ConsoleCommand() {
            @Override
            public void process(String command, String[] args) {
                System.out.println(((GameServerPacket) client.request("127.0.0.1", wrapper.get("127.0.0.1"), new StartGameServerPacket("test", 25566, 10))).getServerName());
            }
        });

        logger.log(language.get("master_started"));
    }

    public static void main(String[] args) throws IOException {
        System.out.println("    __          __             ________                __\n   / /   ____  / /___  _______/ ____/ /___  __  ______/ /\n  / /   / __ \\/ __/ / / / ___/ /   / / __ \\/ / / / __  / \n / /___/ /_/ / /_/ /_/ (__  ) /___/ / /_/ / /_/ / /_/ /  \n/_____/\\____/\\__/\\__,_/____/\\____/_/\\____/\\__,_/\\__,_/\n");

        System.out.println("Master - Copyright (c) 2017 Lennart Heinrich");
        System.out.println("www.lheinrich.com");

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
                System.out.println("Stopping Master...");
                System.exit(0);
            }
        }

        System.out.println("Starting Master...");

        new Master();
    }

    private void loadLanguageFile() {
        String resourceName = "/language_" + config.getString("language", "en") + ".json";

        InputStream stream = null;
        OutputStream resStreamOut = null;

        String jarFolder;

        try {
            jarFolder = new File(Master.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');

            stream = Master.class.getResourceAsStream(resourceName);

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

    private void registerHandler() {
        server.registerHandler("register", new Handler() {
            @Override
            public Packet handle(Packet packet, String client) throws IOException {
                RegisterPacket registerPacket = (RegisterPacket) packet;
                server.acceptIP(client);
                wrapper.put(client, registerPacket.port);
                logger.log(language.get("new_wrapper").replace("$client", client).replace("$port", String.valueOf(registerPacket.port)), LogLevel.WARNING);
                return new RegisteredPacket(true, null, Crypter.encrypt(registerPacket.key, Crypter.toByteArray(server.key), "RSA"));
            }
        });

        MainWebHandler main = new MainWebHandler();
        webServer.registerHandler("style.css", new Style());
        webServer.registerHandler("", main);
        webServer.registerHandler("login", main);
        webServer.registerHandler("dashboard", new Dashboard());
        webServer.registerHandler("groups", new Groups());
        webServer.registerHandler("logout", new Logout());
    }
}
