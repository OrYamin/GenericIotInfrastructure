package server.rps.command;

import com.google.gson.JsonObject;

public interface Command {
    JsonObject execute();
}