package io.dani.cloudkv.core;

import java.util.*;

public final class TxManager {
    private final KVStore store;
    private final Map<UUID, TxContext> txs = new HashMap<>();
    private final Object commitLock = new Object();

    public TxManager(KVStore store) { this.store = store; }

    public UUID begin() {
        UUID id = UUID.randomUUID();
        txs.put(id, new TxContext(id));
        return id;
    }

    private TxContext require(UUID id) {
        TxContext tx = txs.get(id);
        if (tx == null || !tx.open) throw new IllegalStateException("INVALID_TX");
        return tx;
    }

    public void put(UUID id, String key, ValueEntry v) { require(id).overlay.put(key, Optional.of(v)); }
    public void delete(UUID id, String key) { require(id).overlay.put(key, Optional.empty()); }

    public Optional<ValueEntry> get(UUID id, String key) {
        TxContext tx = require(id);
        if (tx.overlay.containsKey(key)) return tx.overlay.get(key);
        return store.get(key);
    }

    public void rollback(UUID id) {
        TxContext tx = require(id);
        tx.open = false;
        tx.overlay.clear();
        txs.remove(id);
    }

    public void commit(UUID id) {
        TxContext tx = require(id);
        synchronized (commitLock) {
            tx.overlay.forEach((k, vOpt) -> {
                if (vOpt.isPresent()) store.put(k, vOpt.get());
                else store.delete(k);
            });
        }
        tx.open = false;
        tx.overlay.clear();
        txs.remove(id);
    }
}
