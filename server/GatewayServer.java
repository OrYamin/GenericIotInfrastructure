package server;

import server.connection.ConnectionService;
import server.connection.http_handlers.IotHTTPHandler;
import server.rps.RequestProcessingService;
import server.rps.command.Command;
import server.rps.dir_monitor.DirWatcher;
import server.rps.plugin_loader.DynamicJarLoader;
import util.ParserIMP;
import util.observer.Subscriber;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.function.Consumer;

public class GatewayServer {
    private final RequestProcessingService rps = new RequestProcessingService(new ParserIMP());
    private final ConnectionService connectionService = new ConnectionService(rps);
    private static final String PATH_TO_PLUGINS_DIR = "/git/projects/iot_infrastructure/plugins/";
    private PluginManager pluginManager;

    public GatewayServer() throws IOException {
        connectionService.addTCPListener(InetAddress.getByName("10.10.0.163"), 50000);
        connectionService.addUDPListener(InetAddress.getByName("10.10.0.163"), 50000);
        rps.startRps();
        connectionService.start();
        pluginManager = new PluginManager();
    }

    public void stopService(){
        connectionService.stop();
        rps.stopService();
        pluginManager.stopManager();
    }

    private class PluginManager extends Subscriber<String> {
        private final DirWatcher dirWatcher;
        private final Thread watcherThread;

        private PluginManager() throws IOException {
            super(new LoadNewCommands(), ()->{});

            dirWatcher = new DirWatcher(PATH_TO_PLUGINS_DIR);
            register(dirWatcher);

            watcherThread = new Thread(()-> {
                try {
                    dirWatcher.startWatchService();
                } catch (InterruptedException ignored){
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            watcherThread.start();

            new LoadNewCommands().accept(PATH_TO_PLUGINS_DIR + "iot_infrastructure.jar"); //for basic commands
        }

        public void stopManager(){
            dirWatcher.stopWatchService();
            watcherThread.interrupt();
            unregister();
        }
    }

    private class LoadNewCommands implements Consumer<String> {
        @Override
        public void accept(String pathToJar) {
            try {
                List<Class<?>> listOfCommands = DynamicJarLoader.load(pathToJar, Command.class.getName());
                List<Class<?>> listOfHandlers = DynamicJarLoader.load(pathToJar, IotHTTPHandler.class.getName());
                rps.addCommands(listOfCommands);
                connectionService.addUrlsToHttpServer(listOfHandlers);
            } catch (IOException | ClassNotFoundException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
