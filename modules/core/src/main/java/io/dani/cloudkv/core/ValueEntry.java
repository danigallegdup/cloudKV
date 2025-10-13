package io.dani.cloudkv.core;

import java.util.Objects;

public final class ValueEntry {
    private final String value;
    private final long createdAtMs;
    private final Long expiresAtMs; // nullable

    public ValueEntry(String value, long createdAtMs, Long expiresAtMs) {
        this.value = Objects.requireNonNull(value);
        this.createdAtMs = createdAtMs;
        this.expiresAtMs = expiresAtMs;
    }
    public String value() { return value; }
    public long createdAtMs() { return createdAtMs; }
    public Long expiresAtMs() { return expiresAtMs; }
    public boolean isExpired(long nowMs) {
        return expiresAtMs != null && nowMs >= expiresAtMs;
    }
}
