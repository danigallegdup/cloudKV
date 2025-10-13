#!/usr/bin/env bash
set -euo pipefail

# cloudKV demo script (MVP targets)
# (Endpoints will exist after later commits.)

echo "1) Start the server (HTTPS by default)"
echo "   ./gradlew :api-rest:run"

echo "2) Put a value"
echo '   curl -sk -X POST "https://localhost:8443/kv?key=greet" --data-binary "hola"'

echo "3) Get it back"
echo '   curl -sk "https://localhost:8443/kv?key=greet"'

echo "4) Begin a transaction, write TTL item, commit"
echo '   TX=$(curl -sk -X POST https://localhost:8443/tx/begin | jq -r .txId)'
echo '   curl -sk -X POST "https://localhost:8443/kv?key=ephemeral&ttl=1000" -H "X-Tx-Id: $TX" --data "temp"'
echo '   curl -sk -X POST https://localhost:8443/tx/commit -H "X-Tx-Id: $TX"'

echo "5) Export HTML"
echo '   curl -sk "https://localhost:8443/export/html" > store.html && (open store.html || xdg-open store.html)'

echo "6) Metrics"
echo '   curl -sk "https://localhost:8443/metrics" | jq'
