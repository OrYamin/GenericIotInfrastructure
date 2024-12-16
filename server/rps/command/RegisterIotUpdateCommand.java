package server.rps.command;

import com.google.gson.JsonObject;
import org.bson.Document;
import server.dbms.MongoDBManager;

public class RegisterIotUpdateCommand implements Command{
    private final int companyId;
    private final int productId;
    private final int iotID;
    private final int updateID;


    public RegisterIotUpdateCommand(JsonObject data){
        companyId = data.get("Company ID").getAsInt();
        productId = data.get("Product ID").getAsInt();
        iotID = data.get("Iot ID").getAsInt();
        updateID = data.get("UpdateID").getAsInt();
    }

    @Override
    public JsonObject execute() {
        MongoDBManager dbManager = MongoDBManager.getInstance();
        String database = "company_" + companyId;
        String collection = "Iots_of_" + productId;
        JsonObject response = new JsonObject();
        try {
            // Check if the product exists in the products collection
            Document filter = new Document("_id", iotID);
            boolean iotExists = dbManager.find(database, collection, filter).first() != null;

            if (!iotExists) {
                response.addProperty("Status", 400);
                response.addProperty("Info", "Iot ID " + iotID + " does not exist.");
                return response;
            }

            // Create the IoT document with iotID as the _id
            Document iotDocument = new Document("_id", updateID);

            // Insert the IoT document into the appropriate collection
            dbManager.insertOne(database, "Updates_of_iot_" + iotID + "_of_product_" + productId, iotDocument);

            response.addProperty("Status", 200);
            response.addProperty("Info", "IoT device update registered successfully.");
        } catch (Exception e) {
            response.addProperty("Status", 400);
            response.addProperty("Info", "An error occurred: " + e.getMessage());
        }

        return response;
    }
}
