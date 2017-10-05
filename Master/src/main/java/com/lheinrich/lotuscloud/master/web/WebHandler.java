package com.lheinrich.lotuscloud.master.web;

import com.lheinrich.lotuscloud.master.main.Master;

import java.util.HashMap;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public abstract class WebHandler {

    public static String getUser(String ip) {
        return Master.instance.webServer.user.get(ip);
    }

    public abstract String process(HashMap<String, String> request, String ip);
}