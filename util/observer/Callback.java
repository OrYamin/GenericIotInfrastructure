package util.observer;

import java.util.function.Consumer;

public class Callback<T> {
    private Consumer<T> consumer;
    private Runnable stopUpdateRunnable;
    private Dispatcher<T> dispatcher = new Dispatcher<>();

    public Callback(Consumer<T> consumer, Runnable stopUpdateRunnable) {
        this.consumer = consumer;
        this.stopUpdateRunnable = stopUpdateRunnable;
    }

    public void update(T data) {
        consumer.accept(data);
    }

    public void stopUpdate() {
        stopUpdateRunnable.run();
        dispatcher = null;
    }

    public void setDispatcher(Dispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void unregister() {
        dispatcher.unregister(this);
        dispatcher = null;
    }
}