package server.connection.http_handlers;

import com.sun.net.httpserver.HttpExchange;

public interface IotHTTPHandler {
    void handle(HttpExchange httpExchange);
    String getPath();
    IotHTTPHandler getHandler();
}
