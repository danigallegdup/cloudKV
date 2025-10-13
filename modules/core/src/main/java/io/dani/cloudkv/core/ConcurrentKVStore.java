package io.dani.cloudkv.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentKVStore implements KVStore {
    private final ConcurrentHashMap<String, ValueEntry> map; // no inline init

    public ConcurrentKVStore() {
        this.map = new ConcurrentHashMap<>();
    }

    public ConcurrentKVStore(ConcurrentHashMap<String, ValueEntry> map) {
        this.map = map;
    }

    /** Expose raw map for TTLService wiring (MVP convenience). */
    public ConcurrentHashMap<String, ValueEntry> raw() {
        return map;
    }

    @Override
    public Optional<ValueEntry> get(String key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public void put(String key, ValueEntry entry) {
        map.put(key, entry);
    }

    @Override
    public void delete(String key) {
        map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }
}
