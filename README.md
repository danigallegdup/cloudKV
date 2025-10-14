# cloudKV

A Concurrent Java Key–Value Store with RESTful, SQL, XML, and HTTPS Infrastructure Features

## About This project

- **Java web server (JAX-RS/Jersey on Jetty)** with **HTTPS**
- **SQL transactions** via **JDBC/H2** on commit
- **XML/XSLT/XQuery** export for enterprise/DITA-style workflows
- **Concurrency**: CHM + TTL sweeper + atomic commit
- **Docs first**: requirements, design, ADRs, demo script

> **cloudKV** is a thread-safe, RESTful, in-memory key–value database built in Java 17.
> It exposes HTTP APIs on port `8080`, supports TTL expiry, transaction-like operations (BEGIN / COMMIT / ROLLBACK), and handles concurrent requests safely — a lightweight Redis-style system implemented from scratch.

---

## 🚀 Quick Start (WSL / Linux)

```bash
# 1. Prerequisites
sudo apt update
sudo apt install -y openjdk-17-jdk curl jq

# 2. Clone and build
git clone https://github.com/youruser/cloudKV.git
cd cloudKV
./gradlew :modules:api-rest:build

# 3. Run the REST server (port 8080)
./gradlew :modules:api-rest:run
# → cloudKV running on http://localhost:8080
```

---

## 🧩 API Showcase

All endpoints are plain HTTP (no HTTPS required in dev).

### 🩺 Health Check

```bash
curl -s http://localhost:8080/health
# → {"status":"UP"}
```

---

### ⚙️ Basic CRUD Operations

```bash
# CREATE
curl -s -X POST "http://localhost:8080/kv?key=foo" \
  -H "Content-Type: text/plain" \
  --data "bar"
# → {"status":"OK"}

# READ
curl -s "http://localhost:8080/kv?key=foo"
# → {"key":"foo","value":"bar","createdAtMs":1760401877958}

# DELETE
curl -s -X DELETE "http://localhost:8080/kv?key=foo"
curl -s "http://localhost:8080/kv?key=foo"
# → {"error":"NOT_FOUND"}
```

---

### ⏱️ TTL (Time-To-Live) Expiration

```bash
# Create a temporary key that expires in 1 second
curl -s -X POST "http://localhost:8080/kv?key=temp&ttl=1000" \
  -H "Content-Type: text/plain" \
  --data "ephemeral"
# → {"status":"OK"}

sleep 2
curl -s "http://localhost:8080/kv?key=temp"
# → {"error":"NOT_FOUND"}  (expired automatically)
```

---

### 💾 Transactions (BEGIN / COMMIT / ROLLBACK)

```bash
# Start a new transaction
TX=$(curl -s -X POST http://localhost:8080/tx/begin | jq -r .txId)
echo $TX
# → e.g. 7d1b66f8-6e47-43ce-9623-f92a5a80df3b

# Stage a write inside TX
curl -s -X POST "http://localhost:8080/tx/put?key=order123" \
  -H "X-Tx-Id: $TX" -H "Content-Type: text/plain" \
  --data "pending"
# → {"status":"OK"}

# Commit it
curl -s -X POST http://localhost:8080/tx/commit -H "X-Tx-Id: $TX"
# → {"status":"COMMITTED"}

# Verify it’s visible globally
curl -s "http://localhost:8080/kv?key=order123"
# → {"key":"order123","value":"pending", ...}
```

#### Rollback example

```bash
TX2=$(curl -s -X POST http://localhost:8080/tx/begin | jq -r .txId)
curl -s -X POST "http://localhost:8080/tx/put?key=tmpRollback" \
  -H "X-Tx-Id: $TX2" -H "Content-Type: text/plain" \
  --data "willDisappear"
curl -s -X POST http://localhost:8080/tx/rollback -H "X-Tx-Id: $TX2"
# → {"status":"ROLLEDBACK"}

curl -s "http://localhost:8080/kv?key=tmpRollback"
# → {"error":"NOT_FOUND"}
```

---

### 🧵 Concurrency Demonstration

```bash
# Spawn 5 parallel writes (simulating concurrent clients)
for i in {1..5}; do
  curl -s -X POST "http://localhost:8080/kv?key=k$i" \
    -H "Content-Type: text/plain" --data "v$i" &
done
wait

# Verify data consistency
curl -s "http://localhost:8080/kv?key=k3"
# → {"key":"k3","value":"v3","createdAtMs":1760402063262}
```

---

## 🧠 What This Demonstrates

| Concept                         | How It’s Shown                                                           |
| ------------------------------- | ------------------------------------------------------------------------ |
| **Client-Server Architecture**  | RESTful API built on Jetty + Jersey                                      |
| **Concurrency & Thread Safety** | `ConcurrentHashMap` + background TTL eviction thread                     |
| **HTTP Standards**              | Correct verbs, status codes, and headers                                 |
| **SQL / ACID Awareness**        | `BEGIN`, `COMMIT`, `ROLLBACK` mimic transactional isolation              |
| **Multi-Threaded Design**       | Multiple parallel requests and TTL cleanup thread                        |
| **XML Stack (optional next)**   | `/export/xml` and `/export/html` endpoints transform store data via XSLT |
| **Observability**               | `/health` endpoint for uptime and monitoring                             |

---

## 🧪 Automated Integration Test

Run all of the above automatically:

```bash
make it
# Builds, runs cloudKV, executes tests, prints ✅ All HTTP MVP integration tests passed.
```

---

## 🦯 Tech Stack

| Layer        | Technology                                | Purpose                                  |
| ------------ | ----------------------------------------- | ---------------------------------------- |
| Core Engine  | `ConcurrentHashMap`, Java 17              | Thread-safe key–value store              |
| Web Server   | Jetty 11 + Jersey 3                       | RESTful API over HTTP/1.1                |
| Transactions | Custom overlay (`TxManager`, `TxContext`) | Simulated ACID behavior                  |
| Concurrency  | `ScheduledExecutorService`                | Background TTL and eviction              |
| Build        | Gradle 9 Kotlin DSL                       | Multi-module setup (`core` + `api-rest`) |
| Testing      | Bash + `curl` + `jq`                      | Integration verification                 |
| CI/CD        | GitHub Actions + Makefile                 | Automated build + test pipeline          |

---

## ✅ Example Output Snapshot

```text
 root@Nayla mnt/..../cloudKV  CI  scripts/it_http_mvp.sh

# HEALTH
RESPONSE: {"status":"UP"}

# CRUD: CREATE
RESPONSE: {"status":"OK"}

# CRUD: READ
RESPONSE: {"key":"foo","value":"bar","createdAtMs":1760478701652}
{
  "key": "foo",
  "value": "bar",
  "createdAtMs": 1760478701652
}

# CRUD: UPDATE
RESPONSE: {"status":"OK"}
{
  "status": "OK"
}
RESPONSE: {"key":"foo","value":"baz","createdAtMs":1760478701764}
{
  "key": "foo",
  "value": "baz",
  "createdAtMs": 1760478701764
}

# CRUD: DELETE
DELETE HTTP CODE: 204
RESPONSE: {"error":"NOT_FOUND"}

# TTL
RESPONSE: {"status":"OK"}
RESPONSE: {"error":"NOT_FOUND"}

# TX: BEGIN -> PUT -> COMMIT
RESPONSE: {"txId":"45752f20-721f-4f79-97e1-6803e37a20e6"}
RESPONSE: {"status":"OK"}
{
  "status": "OK"
}
RESPONSE: {"status":"COMMITTED"}
{
  "status": "COMMITTED"
}
RESPONSE: {"key":"order123","value":"pending","createdAtMs":1760478703974}

# TX: ROLLBACK
RESPONSE: {"txId":"fbb668c0-4696-45f3-a777-ccf021e5015d"}
RESPONSE: {"status":"OK"}
RESPONSE: {"status":"ROLLEDBACK"}
RESPONSE: {"error":"NOT_FOUND"}

# CONCURRENCY: 5 parallel writes
RESPONSE: {"key":"k3","value":"v3","createdAtMs":1760478704138}

✅ All HTTP MVP integration tests passed.
```

---

### 🏁 Summary

`cloudKV` is a compact distributed-systems training ground — proving understanding of:

* Client-server & HTTP/HTTPS architecture
* Concurrency primitives and thread safety
* RESTful design, transactions, and TTL management
* Clean modular build (Gradle 9, multi-module, Java 17)
* Production-style testing via `curl`, `jq`, and GitHub Actions

> **“From key-value semantics to transactional logic — everything happens safely, concurrently, and over real HTTP.”**

---

## 🖨️ Architecture Diagram + Process Flow

```text
           ┌────────────────────────────┐
           │        HTTP Client         │
           │ (curl, Postman, browser)   │
           └──────────────┬─────────────┘
                          │ REST / JSON
           ┌──────────────┴──────────────┐
           │      Jetty + Jersey API     │
           │   (/kv, /tx, /health)       │
           └──────────────┬──────────────┘
                          │ Service calls
         ┌────────────────┴────────────────┐
         │           Service Layer         │
         │ TxManager + TTL Scheduler       │
         └────────────────┬────────────────┘
                          │ Thread-safe access
     ┌────────────────────┴────────────────────┐
     │           ConcurrentKVStore             │
     │ (ConcurrentHashMap<String,ValueEntry>)  │
     └────────────────────┬────────────────────┘
                          │ Optional persistence
              ┌───────────┴────────────┐
              │        SQL/JDBC        │
              └────────────────────────┘

Process Flow:
1. Client sends HTTP requests (`/kv`, `/tx`, `/health`).
2. Jetty routes them to Jersey resources.
3. Resources delegate to core store or transaction manager.
4. TTL background thread expires keys asynchronously.
5. Responses are serialized as JSON and returned to client.
```
