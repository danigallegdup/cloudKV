# cloudKV — Design Overview

## Architecture (high level)

Client → HTTPS (Jetty) → JAX-RS Resources → Services (TxManager, KVStore) → { Core KV (CHM + TTL), Persistence (JDBC/H2), XML (Saxon), Observability }

## Key Components

- **KVStore**: ConcurrentHashMap backing store, ValueEntry{value, createdAt, expiresAt?}
- **TTLService**: Scheduled sweeper (daemon) removing expired keys
- **TxManager**: Tx overlay map per txId; atomic commit under narrow write lock; JDBC batch upsert if enabled
- **REST (JAX-RS/Jersey)**: `/kv`, `/tx/*`, `/export/*`, `/metrics`
- **XML/XSLT/XQuery**: Snapshot committed state to XML; transform to HTML via Saxon; (optional) read-only XQuery
- **Observability**: Structured logs (JSON), request IDs, counters & latency

## Data Flow (PUT in a transaction)

1. Client `POST /tx/begin` → `txId`
2. Client `POST /kv?key=...` (header `X-Tx-Id`) → Tx overlay write
3. Client `POST /tx/commit` → apply overlay to base map under write lock + JDBC transaction

## Error Model (uniform JSON)

`400 INVALID_KEY/TTL`, `404 NOT_FOUND`, `409 TX_CONFLICT`, `413 VALUE_TOO_LARGE`, `500 INTERNAL_ERROR`

## Trade-offs

- **Atomic commit via short write lock**
- keeps reads mostly lock-free
- **Single-node** simplifies correctness; persistence only on commit
- **Saxon** chosen for robust XSLT/XQuery; aligns with DITA/CCMS-style tooling
