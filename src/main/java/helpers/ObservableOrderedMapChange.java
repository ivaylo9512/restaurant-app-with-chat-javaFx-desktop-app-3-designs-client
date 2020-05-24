package helpers;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ObservableOrderedMapChange<K, V> extends MapChangeListener.Change<K,V> {

    private final K key;
    private final V added;
    private int index;

    public ObservableOrderedMapChange(K key, V added, int index, ObservableMap<K, V> map) {
        super(map);
        this.key = key;
        this.added = added;
        this.index = index;
    }

    @Override
    public boolean wasAdded() {
        return true;
    }

    @Override
    public boolean wasRemoved() {
        return false;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValueAdded() {
        return added;
    }

    @Override
    public V getValueRemoved() {
        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
