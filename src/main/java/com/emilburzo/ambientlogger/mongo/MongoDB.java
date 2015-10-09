package com.emilburzo.ambientlogger.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDB {

    private static MongoClient client;

    static {
        if (client == null) {
            client = new MongoClient("mongodb");
        }
    }

    protected MongoDB() {
    }

    public static MongoClient get() {
        return client;
    }

    public static MongoCollection<Document> getReadingCollection() {
        return getAmbientDatabase().getCollection("readings"); // todo
    }

    public static MongoDatabase getAmbientDatabase() {
        return getDatabase("ambient"); // todo
    }

    private static MongoDatabase getDatabase(String db) {
        return get().getDatabase(db);
    }

}
