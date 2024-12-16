package util;

import java.util.ArrayList;
import java.util.Map;

public class Pair<K,V> implements Map.Entry<K,V> {

    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public V setValue(V value) {
        V previousValue = this.value;
        this.value = value;

        return previousValue;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Key: " + key + ", value: " + value;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if(o == null){
            return false;
        }

        if(!(o instanceof Pair)){
            return false;
        }

        return ((Pair<?, ?>) o).getKey().equals(key) && ((Pair<?, ?>) o).getValue().equals(value);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public static <K,V> Pair<V, K> swap(Pair<K, V> pair) {
        return new Pair<>(pair.getValue(), pair.getKey());
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public static <T extends Comparable<T>> Pair<T, T> minmax(ArrayList<T> elements) {
        T min = elements.get(0);
        T max = elements.get(0);
        for(T element : elements){
            if(min.compareTo(element) > 0){
                min = element;
            }

            if(max.compareTo(element) < 0){
                max = element;
            }
        }
        return new Pair<>(min, max);
    }
}