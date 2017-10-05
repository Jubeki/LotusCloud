package com.lheinrich.lotuscloud.master.webhandler;

import com.lheinrich.lotuscloud.master.main.Master;
import com.lheinrich.lotuscloud.master.web.WebHandler;
import java.util.HashMap;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Dashboard extends WebHandler {

    @Override
    public String process(HashMap<String, String> request, String ip) {
        String user = Master.instance.webServer.user.get(ip);
        return "<div class='box'>" +
                "<h2>" + Master.instance.language.get("welcome_back").replace("$username", user) + "</h2>" +
                "</div>";
    }
}
