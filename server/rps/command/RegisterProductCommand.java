package server.rps.command;

import com.google.gson.JsonObject;
import org.bson.Document;
import server.dbms.MongoDBManager;

public class RegisterProductCommand implements Command{
    private final int companyId;
    private final String name;
    private final int productId;

    public RegisterProductCommand(JsonObject data){
        companyId = data.get("Company ID").getAsInt();
        productId = data.get("ProductID").getAsInt();
        name = data.get("Name").getAsString();
    }

    @Override
    public JsonObject execute() {
        JsonObject response = new JsonObject();
        MongoDBManager mongoDBManager = MongoDBManager.getInstance();
        String databaseName = "company_" + companyId;
        String collectionName = "products";

        try {
            // Create the document
            Document document = new Document()
                    .append("_id", productId)
                    .append("name", name);

            // Insert the document into the collection
            mongoDBManager.insertOne(databaseName, collectionName, document);

            // Build the success response
            response.addProperty("Status", 200);
            response.addProperty("Info", "Product registered successfully.");
        } catch (Exception e) {
            // Handle any exceptions
            response.addProperty("Status", 400);
            response.addProperty("Info", "Failed to register product: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}
