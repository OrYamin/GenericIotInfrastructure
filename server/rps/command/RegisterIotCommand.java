package server.rps.command;

import com.google.gson.JsonObject;
import org.bson.Document;
import server.dbms.MongoDBManager;

public class RegisterIotCommand implements Command{
    private final int companyId;
    private final int productId;
    private final int iotID;

    public RegisterIotCommand(JsonObject data){
        companyId = data.get("Company ID").getAsInt();
        productId = data.get("Product ID").getAsInt();
        iotID = data.get("Iot ID").getAsInt();
    }

    @Override
    public JsonObject execute() {
        MongoDBManager dbManager = MongoDBManager.getInstance();
        String database = "company_" + companyId;
        String iotsCollection = "Iots_of_" + productId;
        JsonObject response = new JsonObject();
        try {
            // Check if the product exists in the products collection
            Document filter = new Document("_id", productId);
            boolean productExists = dbManager.find(database, "products", filter).first() != null;

            if (!productExists) {
                response.addProperty("Status", 400);
                response.addProperty("Info", "Product ID " + productId + " does not exist.");
                return response;
            }

            // Create the IoT document with iotID as the _id
            Document iotDocument = new Document("_id", iotID);

            // Insert the IoT document into the appropriate collection
            dbManager.insertOne(database, iotsCollection, iotDocument);

            response.addProperty("Status", 200);
            response.addProperty("Info", "IoT device registered successfully.");
        } catch (Exception e) {
            response.addProperty("Status", 400);
            response.addProperty("Info", "An error occurred: " + e.getMessage());
        }

        return response;
    }
}
