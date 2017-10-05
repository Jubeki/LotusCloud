package com.lheinrich.lotuscloud.master.webhandler;

import com.lheinrich.lotuscloud.master.main.Master;
import com.lheinrich.lotuscloud.master.web.WebHandler;

import java.util.HashMap;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Logout extends WebHandler {

    @Override
    public String process(HashMap<String, String> request, String ip) {
        Master.instance.webServer.user.remove(ip);
        return "<meta http-equiv=\"refresh\" content=\"0; url=/login\" />";
    }
}
