package server.rps.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bson.Document;
import server.dbms.MongoDBManager;

public class GetIotUpdateCommand implements Command{
    private final int companyId;
    private final int productId;
    private final int iotID;
    private final int updateID;

    public GetIotUpdateCommand(JsonObject data){
        companyId = data.get("Company ID").getAsInt();
        productId = data.get("Product ID").getAsInt();
        iotID = data.get("Iot ID").getAsInt();
        updateID = data.get("Update ID").getAsInt();
    }

    @Override
    public JsonObject execute() {
        MongoDBManager dbManager = MongoDBManager.getInstance();
        JsonObject response = new JsonObject();

        try {
            // Create a filter to search for the document with the matching _id
            Document filter = new Document("_id", updateID);

            // Retrieve the document from the collection
            Document iotDocument = dbManager.find("company_" + companyId, "Updates_of_iot_" + iotID + "_of_product_" + productId, filter).first();

            if (iotDocument != null) {
                // Convert the document to JSON format for the response
                response.addProperty("Status", 200);
                response.add("Info", new Gson().fromJson(iotDocument.toJson(), JsonObject.class));
            } else {
                response.addProperty("Status", 400);
                response.addProperty("Info", "IoT device with ID " + iotID + " not found.");
            }
        } catch (Exception e) {
            response.addProperty("Status", 400);
            response.addProperty("Info", "An error occurred: " + e.getMessage());
        }

        return response;
    }
}
