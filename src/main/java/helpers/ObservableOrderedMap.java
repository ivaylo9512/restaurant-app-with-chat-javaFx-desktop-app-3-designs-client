package helpers;

import javafx.beans.InvalidationListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.*;

public class ObservableOrderedMap<K, V> extends ListOrderedMap<K, V> implements ObservableMap<K,V> {
    private Set<MapChangeListener<? super K, ? super V>> listeners = new HashSet<>();

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        V v = super.put(key, value);
        notifyListeners(new ObservableOrderedMapChange<>(key, value, size() - 1, this));

        return v;
    }

    @Override
    public V put(int index, K key, V value) {
        V v = super.put(index, key, value);
        notifyListeners(new ObservableOrderedMapChange<>(key, value, index, this));

        return v;
    }

    @Override
    public V remove(Object key) {
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        super.putAll(m);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return super.entrySet();
    }

    public void notifyListeners(ObservableOrderedMapChange<K, V> change){
        listeners.forEach(mapChangeListener -> mapChangeListener.onChanged(change));
    }
    public void addListener(MapChangeListener<? super K, ? super V> listener) {
        listeners.add(listener);
    }
    public void removeListener(MapChangeListener<? super K, ? super V> listener) {
        listeners.remove(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
    }

    @Override
    public void removeListener(InvalidationListener listener) {
    }
}
