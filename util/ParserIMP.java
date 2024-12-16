package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ParserIMP implements Parser {
    private final Gson gson = new Gson();

    @Override
    public Map.Entry<String, JsonObject> parse(ByteBuffer request) {
        String jsonRequest = new String(request.array()).trim();
        JsonObject jsonObject = gson.fromJson(jsonRequest, JsonObject.class);
        String key = jsonObject.get("Key").getAsString();
        JsonObject data = jsonObject.getAsJsonObject("Data");
        return Pair.of(key, data);
    }

    @Override
    public ByteBuffer serialize(JsonObject response) {
        String jsonResponse = gson.toJson(response);
        return ByteBuffer.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
    }
}