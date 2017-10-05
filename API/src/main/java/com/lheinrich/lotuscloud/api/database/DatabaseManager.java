package com.lheinrich.lotuscloud.api.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import java.util.logging.Level;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class DatabaseManager {

    private MongoDatabase database;
    private MongoClient client;

    public DatabaseManager(String host, int port, String database, String user, String password) {
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        MongoClientURI uri = new MongoClientURI("mongodb://" + user + ":" + password + "@" + host + ":" + port + "/?authSource=" + database);
        client = new MongoClient(uri);
        this.database = getClient().getDatabase(database);
        client.getAddress();
    }

    public MongoClient getClient() {
        return this.client;
    }

    public void disconnect() {
        if (!isConnected())
            client.close();
    }

    public boolean isConnected() {
        try {
            client.getAddress();
        } catch (Exception ex) {
            return false;
        } finally {
            return true;
        }
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }
}