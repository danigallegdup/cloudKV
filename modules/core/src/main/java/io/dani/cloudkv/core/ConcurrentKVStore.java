package io.dani.cloudkv.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentKVStore implements KVStore {
    private final ConcurrentHashMap<String, ValueEntry> map = new ConcurrentHashMap<>();

    @Override public Optional<ValueEntry> get(String key) {
        return Optional.ofNullable(map.get(key));
    }
    @Override public void put(String key, ValueEntry entry) {
        map.put(key, entry);
    }
    @Override public void delete(String key) {
        map.remove(key);
    }
    @Override public int size() { return map.size(); }
}
