package util.observer;

import java.util.function.Consumer;

public class Subscriber<T> {
    private Callback<T> callback;
    private T data;

    public Subscriber(Consumer<T> consumer, Runnable runnable) {
        Consumer<T> saveData = (t)->this.data = t;
        callback = new Callback<>(saveData.andThen(consumer), runnable);
    }

    public void register(Publisher<T> publisher) {
        publisher.register(callback);
    }

    public void unregister() {
        callback.unregister();
    }

    public T getData() {
        return data;
    }
}