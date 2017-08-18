package org.lotuscloud.dnsmanager.main;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lheinrich.jxutils.ReaderUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Copyright (c) 2017 Lennart Heinrich
 * www.lheinrich.com
 */
public class DnsManager {

    public static DnsManager instance;

    public JsonObject config;

    public String cf_zone_id;
    public String cf_api_key;
    public String cf_email;
    public String dns_format;

    public String domain;

    public DnsManager() {
        instance = this;

        loadConfig();

        cf_zone_id = config.getString("cf_zone_id", null);
        cf_api_key = config.getString("cf_api_key", null);

        cf_email = config.getString("cf_email", null);

        dns_format = config.getString("dns_format", "$ip.public.$domain");

        String domainNameRequest = CloudFlareTools.request("dns_records", null, "GET");

        JsonArray array = ReaderUtils.parseJson(domainNameRequest, "result").asArray();

        for (int i = 0; i < array.size(); i++) {
            String resultName = array.get(i).asObject().getString("name", "");

            if (resultName.split("\\.").length == 2) {
                domain = resultName;
                break;
            }
        }

        System.out.println("Detected Domain Name: " + domain);

        new Server(2038).bind();
        new Thread(() -> {
            while (true) try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        new DnsManager();
    }

    private void loadConfig() {
        if (!new File("config.json").exists()) {
            String resourceName = "/config.json";
            InputStream stream = null;
            OutputStream resStreamOut = null;
            String jarFolder;
            try {
                jarFolder = new File(DnsManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');

                stream = DnsManager.class.getResourceAsStream(resourceName);
                if (stream == null) {
                    throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
                }

                int readBytes;
                byte[] buffer = new byte[4096];

                resStreamOut = new FileOutputStream(jarFolder + resourceName);
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Can not load Config: " + ex.getMessage());
            } finally {
                try {
                    stream.close();
                    resStreamOut.close();
                } catch (Exception ex) {
                }
                System.exit(0);
            }
        }

        String configString = null;
        try {
            configString = new String(Files.readAllBytes(new File("config.json").toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = ReaderUtils.parseJson(configString, "config").asObject();
    }
}