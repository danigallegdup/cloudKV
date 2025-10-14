# ===== 0. HEALTH =====
echo "== HEALTH =="
curl -s http://localhost:8080/health
echo -e "\n"

# ===== 1. CRUD (Create/Read/Update/Delete) =====
echo "== CRUD: CREATE/UPDATE =="
curl -s -X POST "http://localhost:8080/kv?key=secure" \
  -H "Content-Type: text/plain" \
  --data "secret"
echo -e "\n"

echo "== CRUD: READ =="
curl -s "http://localhost:8080/kv?key=secure" | jq .
echo -e "\n"

echo "== CRUD: UPDATE value =="
curl -s -X POST "http://localhost:8080/kv?key=secure" \
  -H "Content-Type: text/plain" \
  --data "secret-v2"
echo -e "\n"

echo "== CRUD: READ after update =="
curl -s "http://localhost:8080/kv?key=secure" | jq .
echo -e "\n"

echo "== CRUD: DELETE =="
curl -s -X DELETE "http://localhost:8080/kv?key=secure" -i | head -n1
echo -e "\n"

echo "== CRUD: READ after delete (expect NOT_FOUND) =="
curl -s "http://localhost:8080/kv?key=secure"
echo -e "\n"

# ===== 2. TTL =====
echo "== TTL: put with 1s TTL, then wait and verify eviction =="
curl -s -X POST "http://localhost:8080/kv?key=temp&ttl=1000" \
  -H "Content-Type: text/plain" \
  --data "ephemeral"
sleep 2
curl -s "http://localhost:8080/kv?key=temp"
echo -e "\n"

# ===== 3. Transactions: BEGIN -> PUT -> COMMIT =====
echo "== TX: BEGIN =="
TX=$(curl -s -X POST http://localhost:8080/tx/begin | jq -r .txId)
echo "TX=$TX"
echo -e "\n"

echo "== TX: PUT (staged) =="
curl -s -X POST "http://localhost:8080/tx/put?key=order123" \
  -H "X-Tx-Id: $TX" \
  -H "Content-Type: text/plain" \
  --data "pending"
echo -e "\n"

echo "== TX: COMMIT =="
curl -s -X POST http://localhost:8080/tx/commit -H "X-Tx-Id: $TX"
echo -e "\n"

echo "== VERIFY committed value =="
curl -s "http://localhost:8080/kv?key=order123" | jq .
echo -e "\n"

# ===== 4. Transactions: ROLLBACK =====
echo "== TX: BEGIN 2 =="
TX2=$(curl -s -X POST http://localhost:8080/tx/begin | jq -r .txId)
echo "TX2=$TX2"
echo -e "\n"

echo "== TX: PUT staged (to be rolled back) =="
curl -s -X POST "http://localhost:8080/tx/put?key=tmpRollback" \
  -H "X-Tx-Id: $TX2" \
  -H "Content-Type: text/plain" \
  --data "willDisappear"
echo -e "\n"

echo "== TX: ROLLBACK =="
curl -s -X POST http://localhost:8080/tx/rollback -H "X-Tx-Id: $TX2"
echo -e "\n"

echo "== VERIFY rolled back key (expect NOT_FOUND) =="
curl -s "http://localhost:8080/kv?key=tmpRollback"
echo -e "\n"

# ===== 5. Concurrency smoke =====
echo "== CONCURRENCY: 5 parallel writes =="
for i in 1 2 3 4 5; do
  curl -s -X POST "http://localhost:8080/kv?key=k$i" \
    -H "Content-Type: text/plain" \
    --data "v$i" &
done
wait
echo "== VERIFY one of them =="
curl -s "http://localhost:8080/kv?key=k3" | jq .
echo -e "\n"

# ===== 6. Error cases =====
echo "== ERROR: 400 invalid key (missing key param) =="
curl -s "http://localhost:8080/kv"
echo -e "\n"

echo "== ERROR: 415 missing Content-Type on POST (should fail) =="
curl -s -X POST "http://localhost:8080/kv?key=bad" --data "oops" -i | head -n 10
echo -e "\n"

echo "== DONE =="
