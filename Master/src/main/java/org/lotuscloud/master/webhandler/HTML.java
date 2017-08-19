package org.lotuscloud.master.webhandler;

import org.lotuscloud.master.main.Master;

/**
 * Copyright (c) 2017 Lennart Heinrich
 * www.lheinrich.com
 */
public class HTML {

    public static final String head = "<!DOCTYPE html>" +
            "<head>" +
            "<meta charset='utf-8'>" +
            "<title>LotusCloud.org CP</title>" +
            "<link rel='stylesheet' href='style.css'>" +
            "</head>" +
            "<body>";

    public static final String footer = "</body>" +
            "</html> ";

    public static final String navbar(String active) {
        String[] links = {"groups"};
        String[] right_links = {"logout"};
        StringBuilder builder = new StringBuilder();
        for (String link : links)
            builder.append("<li class='navbar'><a href='" + link + (link.equalsIgnoreCase(active) ? "' class='active'>" : "'>") + Master.instance.language.get(link) + "</a></li>");
        for (String right : right_links)
            builder.append("<li class='navbar nav-right'><a href='" + right + (right.equalsIgnoreCase(active) ? "' class='active'>" : "'>") + Master.instance.language.get(right) + "</a></li>");
        return "<ul class='navbar'><li><a href='dashboard'" + (active.equalsIgnoreCase("dashboard") ? " class='active'>LotusCloud.org</a></li>" : ">LotusCloud.org</a></li>") + builder.toString() + "</ul>";
    }

    public static String redirect(String url) {
        return "<meta http-equiv=\"refresh\" content=\"0; url=" + url + "\" />";
    }
}