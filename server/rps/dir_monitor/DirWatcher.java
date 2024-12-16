package server.rps.dir_monitor;

import util.observer.Publisher;
import java.io.*;
import java.nio.file.*;

public class DirWatcher extends Publisher<String>{
    private final WatchService watchService;
    private Path dirPath;
    private volatile boolean isRunning = true;

    public DirWatcher(String dirPath) throws IOException{
        watchService = FileSystems.getDefault().newWatchService();
        this.dirPath = Paths.get(dirPath);
        if(!this.dirPath.toFile().exists()){
            throw new FileNotFoundException();
        }
        if(!this.dirPath.toFile().isDirectory()){
            throw new IllegalArgumentException();
        }
        this.dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    public void startWatchService() throws InterruptedException, IOException {
        WatchKey key;
        while (isRunning && (key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                    if(event.context().toString().endsWith(".jar")){
                        String jarFilePath = dirPath + "/" + event.context().toString();
                        produce(jarFilePath);
                    }
                }
            }
            key.reset();
        }
        watchService.close();
    }

    public void stopWatchService(){
        isRunning = false;
    }
}