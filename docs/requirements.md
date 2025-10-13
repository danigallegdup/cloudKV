# cloudKV — Requirements

## Objective

A concurrent, single-node key–value store in **Java 17** with **JAX-RS REST API** over **HTTPS**, **TTL eviction**, **transactional overlay + JDBC commit (H2)**, and **XML/XSLT/XQuery** export. Designed to demonstrate Bluestream’s stack: Java web server, web standards, SQL transactions, XML tech, concurrency, and strong documentation.

## In Scope

- Thread-safe in-memory KV (ConcurrentHashMap) with TTL eviction (ScheduledExecutorService)
- Transaction-like overlay: BEGIN/COMMIT/ROLLBACK (atomic commit)
- Optional JDBC persistence on commit (H2 file DB)
- REST endpoints (JAX-RS/Jersey) on Jetty, HTTPS by default
- XML export + XSLT→HTML; optional XQuery (read-only)
- Metrics + structured logs

## Non-Goals

- Clustering/replication, distributed transactions, WAL, auth beyond demo

## Functional Requirements

- CRUD: PUT/GET/DELETE with optional TTL
- Transactions: /tx/begin, /tx/commit, /tx/rollback (header `X-Tx-Id`)
- Export: /export/xml, /export/html (XSLT)
- Metrics: /metrics (requests, hits/misses, p50/p95, uptime)

## Non-Functional

- Concurrency-safe under 100 parallel clients
- Expired entries not served; eviction ≤ sweep+epsilon
- HTTPS by default; HTTP dev-only flag
- Structured logging with request IDs

## Success Criteria (MVP)

- Endpoints behave per spec (HTTP codes, content types)
- TTL works; transactions commit atomically; rollback discards
- H2 persistence on commit (single DB tx)
- XML/XSLT export OK; metrics non-zero; basic concurrency tests pass
