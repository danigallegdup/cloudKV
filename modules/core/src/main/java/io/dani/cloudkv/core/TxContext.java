package io.dani.cloudkv.core;

import java.util.*;

final class TxContext {
    final UUID id;
    final Map<String, Optional<ValueEntry>> overlay = new HashMap<>();
    boolean open = true;
    TxContext(UUID id) { this.id = id; }
}
