package io.dani.cloudkv.api.soap;

import io.dani.cloudkv.core.KVStore;
import io.dani.cloudkv.core.ValueEntry;

import jakarta.jws.WebService;

@WebService(endpointInterface = "io.dani.cloudkv.api.soap.KVSoap")
public class KVSoapImpl implements KVSoap {
    private final KVStore store;

    public KVSoapImpl(KVStore store) { this.store = store; }

    @Override
    public String get(String key) {
        return store.get(key).map(ValueEntry::value).orElse(null);
    }

    @Override
    public String put(String key, String value) {
        long now = System.currentTimeMillis();
        store.put(key, new ValueEntry(value, now, null));
        return "OK";
    }

    @Override
    public boolean delete(String key) {
        // KVStore.delete returns void in core — we'll approximate by checking presence
        boolean exists = store.get(key).isPresent();
        store.delete(key);
        return exists;
    }
}
