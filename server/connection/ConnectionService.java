package server.connection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import server.connection.http_handlers.IotHTTPHandler;
import server.rps.RequestProcessingService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionService {
    private final Selector selector;
    private final Thread selectorThread = new Thread(this::selectionLoop);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final RequestProcessingService rps;
    private boolean hasStarted = false;
    private final HttpService httpService = new HttpService("10.10.0.163", 8000);

    public ConnectionService(RequestProcessingService rps) throws IOException {
        this.rps = rps;
        selector = Selector.open();
    }

    public void addTCPListener(InetAddress address, int port) throws IOException {
        if(hasStarted){
            throw new RejectedExecutionException("Can't add connections after server has started");
        }
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(address, port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new TcpNewConnection(serverSocketChannel));
    }

    public void addUDPListener(InetAddress address, int port) throws IOException {
        if(hasStarted){
            throw new RejectedExecutionException("Can't add connections after server has started");
        }
        DatagramChannel udpChannel = DatagramChannel.open();
        udpChannel.bind(new InetSocketAddress(address, port));
        udpChannel.configureBlocking(false);
        udpChannel.register(selector, SelectionKey.OP_READ, new HandleUDPRead(null, udpChannel));
    }

    public void start(){
        httpService.start();
        hasStarted = true;
        selectorThread.start();
    }

    private void selectionLoop(){
        while(running.get()){
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while(keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    ((KeyHandler) key.attachment()).handle();
                    keyIterator.remove();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop(){
        httpService.stop();
        running.set(false);
        selector.wakeup();
        try {
            selectorThread.join();
            for(SelectionKey key : selector.keys()){
                key.channel().close();
            }
            selector.close();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    /******************************* Event handlers ******************************************************/
    public interface ResponseCreator {
        void respond(ByteBuffer response);
    }

    public interface KeyHandler {
        void handle() throws IOException;
    }

    private class TcpNewConnection implements KeyHandler {
        private final ServerSocketChannel serverSocketChannel;

        public TcpNewConnection(ServerSocketChannel channel){
            serverSocketChannel = channel;
        }

        @Override
        public void handle() throws IOException {
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, new HandleTCPRead(clientChannel));
            System.out.println("TCP client connected." + clientChannel.getRemoteAddress());
        }
    }

    private class HandleTCPRead implements KeyHandler, ResponseCreator {
        private final SocketChannel clientChannel;

        public HandleTCPRead(SocketChannel channel){
            clientChannel = channel;
        }

        @Override
        public void handle() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = clientChannel.read(buffer);
            if(bytesRead == -1){
                clientChannel.close();
                System.out.println("TCP client disconnected.");
            } else {
                buffer.flip();
                rps.handleRequest(buffer, this);
            }
        }

        @Override
        public void respond(ByteBuffer response) {
            try {
                clientChannel.write(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class HandleUDPRead implements KeyHandler, ResponseCreator{
        private final DatagramChannel datagramChannel;
        private final SocketAddress clientAddress;

        public HandleUDPRead(SocketAddress clientAddress, DatagramChannel channel){
            datagramChannel = channel;
            this.clientAddress = clientAddress;
        }

        @Override
        public void handle() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketAddress clientSocket = datagramChannel.receive(buffer);
            buffer.flip();
            rps.handleRequest(buffer, new HandleUDPRead(clientSocket, datagramChannel));
        }

        @Override
        public void respond(ByteBuffer response) {
            try {
                datagramChannel.send(response, clientAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


/*************************************** HTTP Service ******************************************/
    public void addUrlsToHttpServer(List<Class<?>> listOfHandlers) throws NoSuchMethodException {
        for(Class<?> handlerClass : listOfHandlers){
            System.out.println("adding handler to http " + handlerClass.getSimpleName());
            Constructor<?> handlerConstructor = handlerClass.getConstructor(RequestProcessingService.class);
            try {
                IotHTTPHandler handler = (IotHTTPHandler) handlerConstructor.newInstance(rps);
                httpService.addHandler(handler.getPath(), handler.getHandler());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class HttpService {
        private final HttpServer httpServer;
        private final Map<String, IotHTTPHandler> resources = new HashMap<>();

        public HttpService(String ip, int port) {
            try {
                httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getByName(ip), port), 0);
                httpServer.setExecutor(null);
                httpServer.createContext("/", new Router());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void start() {
            httpServer.start();
        }

        public void stop() {
            httpServer.stop(0);
        }

        public void addHandler(String path, IotHTTPHandler handler) {
            resources.put(path, handler);
        }

        private class Router implements HttpHandler{
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                String path = httpExchange.getRequestURI().getPath();
                path = removeIDs(path);
                IotHTTPHandler handler = resources.get(path);
                if(null != handler){
                    handler.handle(httpExchange);
                    return;
                }

                httpExchange.sendResponseHeaders(404, "not found".getBytes().length);
                try(OutputStream os = httpExchange.getResponseBody()){
                    os.write("not found".getBytes());
                }
            }

            private String removeIDs(String path){
                String[] arr = path.split("/");
                StringBuilder ret = new StringBuilder("/");
                for(int i = 1; i < arr.length; i += 2){
                    ret.append(arr[i]).append("/");
                }
                return ret.toString();
            }
        }


    }

    public static class HttpResponder implements ResponseCreator{
        private final HttpExchange httpExchange;

        public HttpResponder(HttpExchange exchange){
            this.httpExchange = exchange;
        }

        @Override
        public void respond(ByteBuffer response) {
            String jsonRequest = new String(response.array()).trim();
            JsonObject jsonObject = new Gson().fromJson(jsonRequest, JsonObject.class);
            int statusCode = jsonObject.get("Status").getAsInt();
            try (OutputStream os = httpExchange.getResponseBody()) {
                httpExchange.getResponseHeaders().add("Content-Type", "application/json");
                httpExchange.sendResponseHeaders(statusCode, response.array().length);
                os.write(response.array());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
