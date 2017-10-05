package com.lheinrich.lotuscloud.master.webhandler;

import com.lheinrich.lotuscloud.master.main.Master;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import com.lheinrich.lotuscloud.master.web.WebHandler;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Style extends WebHandler {

    private String style;

    public Style() {
        String resourceName = "/style.css";
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder = "";
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
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                stream.close();
                resStreamOut.close();
            } catch (Exception ex) {
            }
        }

        try {
            style = new String(Files.readAllBytes(new File(jarFolder, resourceName).toPath()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String process(HashMap<String, String> request, String ip) {
        return style;
    }
}