package io.dani.cloudkv.core;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public final class TTLService implements AutoCloseable {
    private final ConcurrentHashMap<String, ValueEntry> backing;
    private final ScheduledExecutorService ses;
    private final long sweepIntervalMs;

    public TTLService(ConcurrentHashMap<String, ValueEntry> backing, long sweepIntervalMs) {
        this.backing = backing;
        this.sweepIntervalMs = sweepIntervalMs;
        this.ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ttl-sweeper");
            t.setDaemon(true);
            return t;
        });
        this.ses.scheduleAtFixedRate(this::sweep, sweepIntervalMs, sweepIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void sweep() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, ValueEntry>> it = backing.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ValueEntry> e = it.next();
            ValueEntry v = e.getValue();
            if (v != null && v.isExpired(now)) {
                it.remove();
            }
        }
    }

    @Override public void close() { ses.shutdownNow(); }
}
