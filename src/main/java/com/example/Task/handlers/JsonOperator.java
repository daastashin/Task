package com.example.Task.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonOperator {

    public static String getSmthFromReq(String jsonMessage, String key) {
        JsonElement je = JsonParser.parseString(jsonMessage);
        JsonObject jo = je.getAsJsonObject();
        return jo.get(key).getAsString();
    }
}
