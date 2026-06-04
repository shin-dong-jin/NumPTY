package com.numpty.framework.infra;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.numpty.framework.boot.Environment;
import com.numpty.framework.support.AbstractSafeCloseable;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MongoProvider extends AbstractSafeCloseable {

    private static final Logger log = LoggerFactory.getLogger(MongoProvider.class);
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    public MongoProvider() {
        super("Mongo Client");

        MongoCredential mongoCredential = MongoCredential.createCredential(
                Environment.getProperty("mongodb.credential.username"),
                Environment.getProperty("mongodb.credential.database"),
                Environment.getProperty("mongodb.credential.password").toCharArray()
        );
        ConnectionString connectionString = new ConnectionString(Environment.getProperty("mongodb.uri"));

        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .credential(mongoCredential)
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(Environment.getPropertyAsInt("mongodb.conn.timeout", 3_000), TimeUnit.MILLISECONDS)
                        .readTimeout(Environment.getPropertyAsInt("mongodb.read.timeout", 10_000), TimeUnit.MILLISECONDS))
                .applyToClusterSettings(builder -> builder
                        .serverSelectionTimeout(Environment.getPropertyAsInt("mongodb.server_selection.timeout", 3_000), TimeUnit.MILLISECONDS))
                .build();

        this.mongoClient = MongoClients.create(mongoClientSettings);
        this.mongoDatabase = mongoClient.getDatabase(Environment.getProperty("mongodb.database"));
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName);
    }

    @Override
    protected void doClose() {
        if (mongoClient == null) {
            log.info("Mongo Client is either missing or already closed.");
            return;
        }

        try {
            mongoClient.close();
            log.info("Mongo Client is closed.");
        } catch (Exception e) {
            log.error("Failed to close Mongo Client.", e);
        }
    }
}
