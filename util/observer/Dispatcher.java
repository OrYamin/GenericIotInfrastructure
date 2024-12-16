package util.observer;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher<T> {
    private List<Callback<T>> callbackList = new ArrayList<>();

    public void register(Callback<T> cb) {
        callbackList.add(cb);
        cb.setDispatcher(this);
    }

    public void unregister(Callback<T> cb) {
        callbackList.remove(cb);
        cb.stopUpdate();
    }

    public void updateAll(T data) {
        for(Callback<T> callback : callbackList){
            callback.update(data);
        }
    }

    public void stopService() {
        for(Callback<T> callback : callbackList){
            callback.stopUpdate();
        }
        callbackList.clear();
    }
}