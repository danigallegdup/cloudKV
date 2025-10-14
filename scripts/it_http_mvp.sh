#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080"
CURL="curl -sS"

say() { printf "\n# %s\n" "$*"; }
fail() { echo "❌ $*" >&2; exit 1; }
assert_eq() { [ "$1" = "$2" ] || fail "Expected [$2], got [$1] ($3)"; }
assert_json_has_key() { echo "$1" | jq -e "has(\"$2\")" >/dev/null || fail "JSON missing key: $2 in: $1"; }
need() { command -v "$1" >/dev/null || fail "Missing required tool: $1"; }

# --- preflight ---
need curl
need jq

say "HEALTH"
OUT=$($CURL "$BASE_URL/health")
assert_eq "$OUT" '{"status":"UP"}' "health"

say "CRUD: CREATE"
$CURL -X POST "$BASE_URL/kv?key=foo" -H "Content-Type: text/plain" --data-binary "bar" | jq -e '.status=="OK"' >/dev/null

say "CRUD: READ"
OUT=$($CURL "$BASE_URL/kv?key=foo")
echo "$OUT" | jq .
assert_json_has_key "$OUT" "key"
assert_json_has_key "$OUT" "value"

say "CRUD: UPDATE"
$CURL -X POST "$BASE_URL/kv?key=foo" -H "Content-Type: text/plain" --data "baz" | jq -e '.status=="OK"' >/dev/null
OUT=$($CURL "$BASE_URL/kv?key=foo")
echo "$OUT" | jq .
[[ "$(echo "$OUT" | jq -r .value)" == "baz" ]] || fail "update failed"

say "CRUD: DELETE"
# DELETE should be 204 — capture HTTP status only
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/kv?key=foo")
assert_eq "$CODE" "204" "delete status"
OUT=$($CURL "$BASE_URL/kv?key=foo")
assert_eq "$OUT" '{"error":"NOT_FOUND"}' "deleted key not found"

say "TTL"
$CURL -X POST "$BASE_URL/kv?key=temp&ttl=1000" -H "Content-Type: text/plain" --data "ephemeral" | jq -e '.status=="OK"' >/dev/null
sleep 2
OUT=$($CURL "$BASE_URL/kv?key=temp")
assert_eq "$OUT" '{"error":"NOT_FOUND"}' "ttl expired"

say "TX: BEGIN -> PUT -> COMMIT"
TX=$($CURL -X POST "$BASE_URL/tx/begin" | jq -r .txId)
$CURL -X POST "$BASE_URL/tx/put?key=order123" -H "X-Tx-Id: $TX" -H "Content-Type: text/plain" --data "pending" | jq -e '.status=="OK"' >/dev/null
$CURL -X POST "$BASE_URL/tx/commit" -H "X-Tx-Id: $TX" | jq -e '.status=="COMMITTED"' >/dev/null
OUT=$($CURL "$BASE_URL/kv?key=order123")
[[ "$(echo "$OUT" | jq -r .value)" == "pending" ]] || fail "tx commit not visible"

say "TX: ROLLBACK"
TX2=$($CURL -X POST "$BASE_URL/tx/begin" | jq -r .txId)
$CURL -X POST "$BASE_URL/tx/put?key=tmpRollback" -H "X-Tx-Id: $TX2" -H "Content-Type: text/plain" --data "willDisappear" | jq -e '.status=="OK"' >/dev/null
$CURL -X POST "$BASE_URL/tx/rollback" -H "X-Tx-Id: $TX2" | jq -e '.status=="ROLLEDBACK"' >/dev/null
OUT=$($CURL "$BASE_URL/kv?key=tmpRollback")
assert_eq "$OUT" '{"error":"NOT_FOUND"}' "tx rollback not applied"

say "CONCURRENCY: 5 parallel writes"
for i in 1 2 3 4 5; do
  $CURL -X POST "$BASE_URL/kv?key=k$i" -H "Content-Type: text/plain" --data "v$i" >/dev/null &
done
wait
OUT=$($CURL "$BASE_URL/kv?key=k3")
[[ "$(echo "$OUT" | jq -r .value)" == "v3" ]] || fail "concurrency write missing"

echo -e "\n✅ All HTTP MVP integration tests passed."
