package server.rps;

import com.google.gson.JsonObject;
import server.connection.ConnectionService;
import server.rps.dir_monitor.DirWatcher;
import server.rps.executor.ThreadPool;
import server.rps.factory.Factory;
import server.rps.command.*;
import server.rps.plugin_loader.DynamicJarLoader;
import util.Parser;
import util.observer.Subscriber;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RequestProcessingService {
    private ThreadPool threadPool;
    private final Parser parser;
    private final Factory<String, Command, JsonObject> commandFactory = new Factory<>();

    public RequestProcessingService(Parser parser) {
        this.parser = parser;
    }

    public void startRps() throws IOException {
        threadPool = new ThreadPool();
    }

    public void handleRequest(ByteBuffer request, ConnectionService.ResponseCreator responder){
        threadPool.submit(new RequestHandler(request, responder));
    }

    private class RequestHandler implements Runnable{
        private final ByteBuffer request;
        private final ConnectionService.ResponseCreator responder;

        public RequestHandler(ByteBuffer request, ConnectionService.ResponseCreator responder){
            this.request = request;
            this.responder = responder;
        }

        @Override
        public void run() {
            JsonObject response = new JsonObject();
            try {
                Map.Entry<String, JsonObject> result = parser.parse(request);
                Command command = commandFactory.create(result.getKey(), result.getValue());
                response = command.execute();
                responder.respond(parser.serialize(response));
            } catch (Exception e){
                response.addProperty("Status", 400);
                response.addProperty("Info", "bad request");
                responder.respond(parser.serialize(response));
            }
        }
    }

    public void stopService(){
        threadPool.shutdown();
    }


    /***************************** plug & play service *****************************************/

    public void addCommands(List<Class<?>> listOfCommands) throws NoSuchMethodException{
        for(Class<?> commandClass : listOfCommands){
            System.out.println("adding command " + commandClass.getSimpleName());
            Constructor<?> commandConstructor = commandClass.getConstructor(JsonObject.class);
            Function<JsonObject, Command> recipe = (data)->{
                try {
                    return (Command) commandConstructor.newInstance(data);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            };
            commandFactory.add(commandClass.getSimpleName(), recipe);
        }
    }
}

