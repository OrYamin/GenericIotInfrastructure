package server.rps.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Factory<K, T, D> {
    private Map<K, Function<D, ? extends T>> factories = new HashMap<>();

    public T create(K key, D data){
        return factories.get(key).apply(data);
    }

    public void add(K key, Function<D, ? extends T> function){
        factories.put(key, function);
    }
}
