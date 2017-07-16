package org.lotuscloud.master.webhandler;

import org.lotuscloud.master.main.Master;
import org.lotuscloud.master.web.WebHandler;

import java.util.HashMap;

/**
 * Copyright (c) 2017 Lennart Heinrich
 * www.lennarth.com
 */
public class Dashboard extends WebHandler {

    @Override
    public String process(HashMap<String, String> request, String ip) {
        String user = Master.instance.webServer.user.get(ip);
        return "<div class='box'>" +
                "<h2>" + Master.instance.language.get("welcome_back").replace("$username", user) + "</h2>" +
                "<br>" +
                "<a href='groups'>" + Master.instance.language.get("groups") + "</a>" +
                "</div>";
    }
}
