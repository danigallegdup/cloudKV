package io.dani.cloudkv.core;

import java.util.Optional;

public interface KVStore {
    Optional<ValueEntry> get(String key);
    void put(String key, ValueEntry entry);
    void delete(String key);
    int size();
}
