package com.lheinrich.lotuscloud.master.webhandler;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.lheinrich.lotuscloud.api.database.DBTools;
import com.lheinrich.lotuscloud.master.main.Master;

import java.util.HashMap;
import java.util.regex.Pattern;
import com.lheinrich.lotuscloud.master.web.WebHandler;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Groups extends WebHandler {

    @Override
    public String process(HashMap<String, String> request, String ip) {
        String user = Master.instance.webServer.user.get(ip);

        if (request.containsKey("name") && request.containsKey("ram")) {
            MongoCollection col = Master.instance.databaseManager.getDatabase().getCollection("groups");
            FindIterable<Document> find = col.find(new BasicDBObject("name", DBTools.noCase(request.get("name"))));

            if (find.first() == null) {
                Document doc = new Document();
                doc.put("name", request.get("name"));
                doc.put("ram", Integer.valueOf(request.get("ram")));

                col.insertOne(doc);

                return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2><p style='color:green;'>" + Master.instance.language.get("add_success") + "</p></div>";
            } else
                return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2><p style='color:red;'>" + Master.instance.language.get("add_exists") + "</p></div>";
        } else if (request.containsKey("add")) {
            String form = "<form method='get'><input placeholder='" + Master.instance.language.get("name") + "' name='name'><br><input placeholder='" + Master.instance.language.get("ram") + "' name='ram' type='number'><br><button type='submit'>" + Master.instance.language.get("submit") + "</button></form>";

            return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2>" + form + "</div>";
        } else if (request.containsKey("ram") && request.containsKey("edit")) {
            MongoCollection col = Master.instance.databaseManager.getDatabase().getCollection("groups");

            Pattern noCase = DBTools.noCase(request.get("edit"));
            FindIterable<Document> find = col.find(new BasicDBObject("name", noCase));

            if (find.first() != null) {
                Document doc = find.first();

                if (doc.containsKey("ram"))
                    doc.remove("ram");

                doc.put("ram", Integer.valueOf(request.get("ram")));

                col.updateOne(Filters.eq("name", noCase), new Document("$set", doc));

                return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2><p style='color:green;'>" + Master.instance.language.get("edit_success") + "</p></div>";
            } else
                return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2><p style='color:red;'>" + Master.instance.language.get("edit_not_exists") + "</p></div>";
        } else if (request.containsKey("edit")) {
            MongoCollection col = Master.instance.databaseManager.getDatabase().getCollection("groups");
            FindIterable<Document> find = col.find(new BasicDBObject("name", DBTools.noCase(request.get("edit"))));

            if (find.first() == null)
                return HTML.redirect("/groups");

            Document doc = find.first();

            String form = "<form method='get'><input name='edit' type='hidden' value='" + doc.getString("name") + "'><input placeholder='" + Master.instance.language.get("ram") + "' name='ram' type='number' value='" + doc.getInteger("ram") + "'><br><button type='submit'>" + Master.instance.language.get("submit") + "</button></form>";

            return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2>" + form + "</div>";
        } else {
            MongoCollection col = Master.instance.databaseManager.getDatabase().getCollection("groups");
            FindIterable<Document> find = col.find();
            StringBuilder builder = new StringBuilder();

            for (Document doc : find) {
                String name = doc.getString("name");
                if (name != null && !name.replace(" ", "").equalsIgnoreCase(""))
                    builder.append("<div class='group'><p>" + doc.getString("name") + "</p><p>" + doc.getInteger("ram") + " MB</p><a href='?edit=" + doc.getString("name") + "'><button>" + Master.instance.language.get("edit") + "</button></a></div>");
            }

            return "<div class='box'><h2>" + Master.instance.language.get("groups") + "</h2>" + builder.toString() + "<br><br><a href='?add=true'><button>" + Master.instance.language.get("add") + "</button></a></div>";
        }
    }
}