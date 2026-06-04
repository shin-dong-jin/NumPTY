package com.numpty.framework.infra.api;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.infra.MongoProvider;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoTemplate {

    private static final Logger log = LoggerFactory.getLogger(MongoTemplate.class);
    private final MongoProvider mongoProvider;

    public MongoTemplate(MongoProvider mongoProvider) {
        this.mongoProvider = mongoProvider;
    }

    public boolean exists(String collectionName, Bson filter) {
        try {
            return mongoProvider.getCollection(collectionName)
                .find(filter)
                .projection(new Document("_id", 1))
                .limit(1)
                .first() != null;
        } catch (MongoException e) {
            throw new CoreException("Error occurred while finding exists: " + collectionName, e);
        }
    }

    public long count(String collectionName, Bson filter) {
        try {
            return mongoProvider.getCollection(collectionName).countDocuments(filter);
        } catch (MongoException e) {
            throw new CoreException("Error occurred while counting tuples: " + collectionName, e);
        }
    }

    public <T> T findOne(String collectionName, Bson filter, DocumentMapper<T> mapper) {
        try {
            Document doc = mongoProvider.getCollection(collectionName).find(filter).first();
            return doc == null ? null : mapper.map(doc);
        } catch (MongoException e) {
            throw new CoreException("Error occurred while finding tuple: " + collectionName, e);
        }
    }

    public <T> List<T> findMany(String collectionName, Bson filter, DocumentMapper<T> mapper) {
        return findMany(collectionName, filter, FindOptions.empty(), mapper);
    }

    public <T> List<T> findMany(String collectionName, Bson filter, FindOptions options,
        DocumentMapper<T> mapper) {
        try {
            FindIterable<Document> find = mongoProvider.getCollection(collectionName).find(filter);

            if (options.sort() != null) {
                find = find.sort(options.sort());
            }

            if (options.skip() != null) {
                find = find.skip(options.skip());
            }

            if (options.limit() != null) {
                find = find.limit(options.limit());
            }

            List<T> results = new ArrayList<>();

            for (Document document : find) {
                results.add(mapper.map(document));
            }

            return results;
        } catch (MongoException e) {
            throw new CoreException("Error occurred while finding tuples: " + collectionName, e);
        }
    }

    public Document insertOne(String collectionName, Document document) {
        try {
            mongoProvider.getCollection(collectionName).insertOne(document);
            return document;
        } catch (MongoException e) {
            throw new CoreException("Error occurred while inserting tuple: " + collectionName, e);
        }
    }

    public List<Document> insertMany(String collectionName, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        try {
            mongoProvider.getCollection(collectionName).insertMany(documents);
            return documents;
        } catch (MongoException e) {
            throw new CoreException("Error occurred while inserting tuples: " + collectionName, e);
        }
    }

    public long updateOne(String collectionName, Bson filter, Bson update) {
        try {
            return mongoProvider.getCollection(collectionName)
                .updateOne(filter, update)
                .getModifiedCount();
        } catch (MongoException e) {
            throw new CoreException("Error occurred while updating tuple: " + collectionName, e);
        }
    }

    public long updateMany(String collectionName, Bson filter, Bson update) {
        try {
            return mongoProvider.getCollection(collectionName)
                .updateMany(filter, update)
                .getModifiedCount();
        } catch (MongoException e) {
            throw new CoreException("Error occurred while updating tuples: " + collectionName, e);
        }
    }

    public long deleteOne(String collectionName, Bson filter) {
        try {
            return mongoProvider.getCollection(collectionName)
                .deleteOne(filter)
                .getDeletedCount();
        } catch (MongoException e) {
            throw new CoreException("Error occurred while deleting tuple: " + collectionName, e);
        }
    }

    public long deleteMany(String collectionName, Bson filter) {
        try {
            return mongoProvider.getCollection(collectionName)
                .deleteMany(filter)
                .getDeletedCount();
        } catch (MongoException e) {
            throw new CoreException("Error occurred while deleting tuples: " + collectionName, e);
        }
    }
}
