package com.lheinrich.lotuscloud.master.webhandler;

import com.lheinrich.lotuscloud.api.crypt.Crypter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.lheinrich.lotuscloud.api.database.DBTools;
import com.lheinrich.lotuscloud.master.main.Master;
import com.lheinrich.lotuscloud.master.web.WebHandler;

import java.util.HashMap;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class MainWebHandler extends WebHandler {

    @Override
    public String process(HashMap<String, String> request, String ip) {
        if (request.containsKey("name") && request.containsKey("password")) {
            MongoCollection collection = Master.instance.databaseManager.getDatabase().getCollection("cp_user");
            DBObject ref = new BasicDBObject();
            ref.put("name", DBTools.noCase(request.get("name")));
            ref.put("password", DBTools.noCase(Crypter.hash("SHA-512", request.get("password"))));
            FindIterable iterable = collection.find((Bson) ref);
            if (iterable.first() == null)
                return "<h3>" + Master.instance.language.get("wrong_login") + "</h3>";
            else {
                Master.instance.webServer.user.put(ip, ((Document) iterable.first()).getString("name"));
                return "<meta http-equiv=\"refresh\" content=\"0; url=/dashboard\" />";
            }
        } else if (Master.instance.webServer.user.containsKey(ip))
            return "<meta http-equiv=\"refresh\" content=\"0; url=/dashboard\" />";
        return "<form method='get'>" +
                "<div class='login'>" +
                "<h3>" + Master.instance.language.get("login") + "</h3>" +
                "<input placeholder='" + Master.instance.language.get("username") + "' name='name' required>" +
                "<input placeholder='" + Master.instance.language.get("password") + "' name='password' required>" +
                "<br><br>" +
                "<button type='submit'>" + Master.instance.language.get("login") + "</button>" +
                "</div>" +
                "</form>";
    }
}