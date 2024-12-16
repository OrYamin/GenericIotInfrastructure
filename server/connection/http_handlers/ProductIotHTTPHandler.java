package server.connection.http_handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import server.connection.ConnectionService;
import server.rps.RequestProcessingService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProductIotHTTPHandler implements IotHTTPHandler{
    private final RequestProcessingService rps;
    private final Map<String, Consumer<HttpExchange>> handlers = new HashMap<>();
    private final Gson gson = new Gson();

    public ProductIotHTTPHandler(RequestProcessingService rps){
        this.rps = rps;
        initHandlers();
    }

    private void initHandlers() {
        handlers.put("GET", (exchange)-> {
            String path = exchange.getRequestURI().getPath();
            String[] arr = path.split("/");
            if (arr.length != 5) {
                try {
                    exchange.sendResponseHeaders(400, "invalid request".length());
                    exchange.getResponseBody().write("invalid request".getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            int companyId = Integer.parseInt(arr[2]);
            int productId = Integer.parseInt(arr[4]);
            JsonObject request = new JsonObject();
            request.addProperty("Key", "GetProductCommand");
            JsonObject data = new JsonObject();
            data.addProperty("Company ID", companyId);
            data.addProperty("Product ID", productId);
            request.add("Data", data);
            String jsonRequest = gson.toJson(request);
            rps.handleRequest(ByteBuffer.wrap(jsonRequest.getBytes()), new ConnectionService.HttpResponder(exchange));
        });

        handlers.put("POST", (exchange)->{
            String path = exchange.getRequestURI().getPath();
            String[] arr = path.split("/");
            if(arr.length != 4){
                try {
                    exchange.sendResponseHeaders(400, "invalid request".length());
                    exchange.getResponseBody().write("invalid request".getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try (InputStream inputStream = exchange.getRequestBody()){
                JsonObject body = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject request = new JsonObject();
                request.addProperty("Key", "RegisterProductCommand");
                body.addProperty("Company ID", arr[2]);
                request.add("Data", body);
                String jsonRequest = gson.toJson(request);
                rps.handleRequest(ByteBuffer.wrap(jsonRequest.getBytes()), new ConnectionService.HttpResponder(exchange));
            } catch (Exception e){
                try {
                    exchange.sendResponseHeaders(400, "invalid request".length());
                    exchange.getResponseBody().write("invalid request".getBytes());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            handlers.get(httpExchange.getRequestMethod()).accept(httpExchange);
        } catch (Exception e){
            try {
                httpExchange.sendResponseHeaders(400, "invalid request".length());
                httpExchange.getResponseBody().write("invalid request".getBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public String getPath() {
        return "/company/product/";
    }

    @Override
    public IotHTTPHandler getHandler() {
        return this;
    }
}
