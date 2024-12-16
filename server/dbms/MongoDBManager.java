package server.dbms;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.List;

public class MongoDBManager {
    private final String connectionString = "mongodb+srv://oryamin25:1234@iot.egay0.mongodb.net/?retryWrites=true&w=majority&appName=Iot";
    private final MongoClient mongoClient;

    private MongoDBManager() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        mongoClient = MongoClients.create(settings);
    }

    // Check if a database exists
    public boolean isDBExists(String databaseName) {
        for (String dbName : mongoClient.listDatabaseNames()) {
            if (dbName.equalsIgnoreCase(databaseName)) {
                return true;
            }
        }
        return false;
    }

    // Check if a collection exists in a database
    public boolean isCollectionExists(String databaseName, String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        for (String collName : database.listCollectionNames()) {
            if (collName.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    // Insert a single document into a collection
    public void insertOne(String databaseName, String collectionName, Document document) {
        MongoCollection<Document> collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
        collection.insertOne(document);
        System.out.println("Inserted document: " + document.toJson());
    }

    // Insert multiple documents into a collection
    public void insertMany(String databaseName, String collectionName, List<Document> documents) {
        MongoCollection<Document> collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
        collection.insertMany(documents);
        System.out.println("Inserted documents: " + documents.size());
    }

    // Find documents with a filter
    public FindIterable<Document> find(String databaseName, String collectionName, Document filter) {
        MongoCollection<Document> collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
        return collection.find(filter);
    }

    // Find documents with a filter and limit the result
    public FindIterable<Document> find(String databaseName, String collectionName, Document filter, int limit) {
        MongoCollection<Document> collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
        return collection.find(filter).limit(limit);
    }

    // Delete a single document from a collection
    public void deleteOne(String databaseName, String collectionName, Document filter) {
        MongoCollection<Document> collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
        collection.deleteOne(filter);
        System.out.println("Deleted one document with filter: " + filter.toJson());
    }

    // Delete multiple documents from a collection
    public void deleteMany(String databaseName, String collectionName, Document filter) {
        MongoCollection<Document> collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
        collection.deleteMany(filter);
        System.out.println("Deleted documents with filter: " + filter.toJson());
    }

    // Close the MongoDB connection
    public void close() {
        mongoClient.close();
        System.out.println("MongoDB connection closed.");
    }


    public static MongoDBManager getInstance(){
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder{
        private static final MongoDBManager INSTANCE = new MongoDBManager();
    }
}
