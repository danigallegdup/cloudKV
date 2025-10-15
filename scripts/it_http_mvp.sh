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
echo "RESPONSE: $OUT"
assert_eq "$OUT" '{"status":"UP"}' "health"

say "CRUD: CREATE"
CREATE_OUT=$($CURL -sS -X POST "$BASE_URL/kv?key=foo" -H "Content-Type: text/plain" --data-binary "bar")
echo "RESPONSE: $CREATE_OUT"
echo "$CREATE_OUT" | jq -e '.status=="OK"' >/dev/null

say "CRUD: READ"
OUT=$($CURL "$BASE_URL/kv?key=foo")
echo "RESPONSE: $OUT"
echo "$OUT" | jq .
assert_json_has_key "$OUT" "key"
assert_json_has_key "$OUT" "value"

say "CRUD: UPDATE"
UPDATE_OUT=$($CURL -sS -X POST "$BASE_URL/kv?key=foo" -H "Content-Type: text/plain" --data "baz")
echo "RESPONSE: $UPDATE_OUT"
echo "$UPDATE_OUT" | jq . || true
OUT=$($CURL "$BASE_URL/kv?key=foo")
echo "RESPONSE: $OUT"
echo "$OUT" | jq .
[[ "$(echo "$OUT" | jq -r .value)" == "baz" ]] || fail "update failed"

say "CRUD: DELETE"
# DELETE should be 204 — capture HTTP status only
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/kv?key=foo")
echo "DELETE HTTP CODE: $CODE"
assert_eq "$CODE" "204" "delete status"
OUT=$($CURL "$BASE_URL/kv?key=foo")
echo "RESPONSE: $OUT"
assert_eq "$OUT" '{"error":"NOT_FOUND"}' "deleted key not found"

say "TTL"
TTL_OUT=$($CURL -sS -X POST "$BASE_URL/kv?key=temp&ttl=1000" -H "Content-Type: text/plain" --data "ephemeral")
echo "RESPONSE: $TTL_OUT"
echo "$TTL_OUT" | jq -e '.status=="OK"' >/dev/null
sleep 2
OUT=$($CURL "$BASE_URL/kv?key=temp")
echo "RESPONSE: $OUT"
assert_eq "$OUT" '{"error":"NOT_FOUND"}' "ttl expired"

say "TX: BEGIN -> PUT -> COMMIT"
TX_JSON=$($CURL -sS -X POST "$BASE_URL/tx/begin")
echo "RESPONSE: $TX_JSON"
TX=$(echo "$TX_JSON" | jq -r .txId)
CURL_OUT=$($CURL -sS -X POST "$BASE_URL/tx/put?key=order123" -H "X-Tx-Id: $TX" -H "Content-Type: text/plain" --data "pending")
echo "RESPONSE: $CURL_OUT"
echo "$CURL_OUT" | jq . || true
COMMIT_OUT=$($CURL -sS -X POST "$BASE_URL/tx/commit" -H "X-Tx-Id: $TX")
echo "RESPONSE: $COMMIT_OUT"
echo "$COMMIT_OUT" | jq . || true
OUT=$($CURL "$BASE_URL/kv?key=order123")
echo "RESPONSE: $OUT"
[[ "$(echo "$OUT" | jq -r .value)" == "pending" ]] || fail "tx commit not visible"

say "TX: ROLLBACK"
TX2_JSON=$($CURL -sS -X POST "$BASE_URL/tx/begin")
echo "RESPONSE: $TX2_JSON"
TX2=$(echo "$TX2_JSON" | jq -r .txId)
PUT2_OUT=$($CURL -sS -X POST "$BASE_URL/tx/put?key=tmpRollback" -H "X-Tx-Id: $TX2" -H "Content-Type: text/plain" --data "willDisappear")
echo "RESPONSE: $PUT2_OUT"
ROLLBACK_OUT=$($CURL -sS -X POST "$BASE_URL/tx/rollback" -H "X-Tx-Id: $TX2")
echo "RESPONSE: $ROLLBACK_OUT"
OUT=$($CURL "$BASE_URL/kv?key=tmpRollback")
echo "RESPONSE: $OUT"
assert_eq "$OUT" '{"error":"NOT_FOUND"}' "tx rollback not applied"

say "CONCURRENCY: 5 parallel writes"
for i in 1 2 3 4 5; do
  CONC_OUT=$($CURL -sS -X POST "$BASE_URL/kv?key=k$i" -H "Content-Type: text/plain" --data "v$i") || true &
done
wait
OUT=$($CURL "$BASE_URL/kv?key=k3")
echo "RESPONSE: $OUT"
[[ "$(echo "$OUT" | jq -r .value)" == "v3" ]] || fail "concurrency write missing"

echo -e "\n✅ All HTTP MVP integration tests passed."

say "XQUERY: select entries with key prefix k"
ENC=$(python3 - <<'PY'
import urllib.parse;print(urllib.parse.quote("for $e in /store/entry[starts-with(@key,'k')] return $e"))
PY
)
XQ_OUT=$($CURL "${BASE_URL}/export/query?xq=${ENC}" || true)
echo "RESPONSE: $XQ_OUT"
echo "$XQ_OUT" | grep "<entry" >/dev/null || fail "xquery did not return entries"

# Optional SOAP WSDL check if enabled
if [ "${SOAP_ENABLED:-}" = "true" ] || [ "${SOAP_ENABLED:-}" = "1" ]; then
  say "SOAP: WSDL"
  WSDL=$($CURL "http://localhost:8090/soap/kv?wsdl" || true)
  echo "RESPONSE: $WSDL"
  echo "$WSDL" | grep -i "definitions" >/dev/null || fail "soap wsdl missing definitions"
fi
