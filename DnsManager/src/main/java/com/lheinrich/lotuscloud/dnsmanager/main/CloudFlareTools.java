package org.lotuscloud.dnsmanager.main;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lheinrich.jxutils.ReaderUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Copyright (c) 2017 Lennart Heinrich
 * www.lheinrich.com
 */
public class CloudFlareTools {

    public static String request(String get, String body, String method) {
        System.out.println("get=" + get);
        System.out.println("body=" + body);
        String response = null;

        URL url;

        try {
            url = new URL("https://api.cloudflare.com/client/v4/zones/" + DnsManager.instance.cf_zone_id + "/" + get);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod(method);

            con.setRequestProperty("X-Auth-Email", DnsManager.instance.cf_email);
            con.setRequestProperty("X-Auth-Key", DnsManager.instance.cf_api_key);

            con.setRequestProperty("Content-Type", "application/json");


            if (body != null) {
                con.setDoOutput(true);
                con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Request Failed. HTTP Error Code: " + con.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuffer jsonString = new StringBuffer();
            String line;

            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }

            response = jsonString.toString();

            br.close();
            con.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private static void updateRecord(String record, String newIp, String type) {
        String oldIp = "null";
        String id = null;

        String domainNameRequest = CloudFlareTools.request("dns_records", null, "GET");

        JsonArray array = ReaderUtils.parseJson(domainNameRequest, "result").asArray();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).asObject();

            String resultName = object.getString("name", "");
            String resultId = object.getString("id", "");
            String resultType = object.getString("type", "");

            if (resultName.equals(record) && resultType.equals(type)) {
                id = resultId;
                oldIp = object.getString("content", "");
                break;
            }
        }

        if (id == null || !newIp.equals(oldIp)) {
            CloudFlareTools.request(id == null ? "dns_records" : "dns_records/" + id, "{\"type\":\"" + type + "\",\"name\":\"" + record + "\",\"content\": \"" + newIp + "\",\"ttl\":120,\"proxied\":false}", id == null ? "POST" : "PUT");
            System.out.println("Updated Record");
        }
    }

    public static void updateDns(String ip, String newIp, int port) {
        String oldIp = "null";
        String subDomain = DnsManager.instance.dns_format.replace("$ip", ip).replace("$domain", DnsManager.instance.domain);
        String record = "_minecraft._tcp." + subDomain;
        String id = null;

        String domainNameRequest = CloudFlareTools.request("dns_records", null, "GET");

        JsonArray array = ReaderUtils.parseJson(domainNameRequest, "result").asArray();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).asObject();

            String resultName = object.getString("name", "");
            String resultId = object.getString("id", "");
            String resultType = object.getString("type", "");

            if (resultName.equals(record) && resultType.equals("SRV")) {
                id = resultId;
                oldIp = object.getString("content", "").split(" ")[3];
                break;
            }
        }

        updateRecord("direct." + subDomain, newIp, "A");

        if (id == null || !newIp.equals(oldIp)) {
            CloudFlareTools.request(id == null ? "dns_records" : "dns_records/" + id, "{\"type\":\"SRV\",\"name\":\"" + record + "\",\"content\": \"0 5 " + port + " direct." + subDomain + "\",\"ttl\":120,\"proxied\":false,\"data\":{\"priority\":0,\"weight\":5,\"port\":" + port + ",\"target\":\"direct." + subDomain + "\",\"service\":\"_minecraft\",\"proto\":\"_tcp\",\"name\":\"" + subDomain + "\"}}", id == null ? "POST" : "PUT");
            System.out.println("Updated DNS");
        }
    }
}