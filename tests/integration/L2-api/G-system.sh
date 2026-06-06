#!/usr/bin/env bash
# tests/integration/L2-api/G-system.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-G: 系统设置测试 ==="
login

R=$(get "/admin/system/monitor/stats" "$ADMIN_TOKEN")
assert_code "G04 系统监控" "0" "$R"

# G05: GET /api/system/ping - returns raw map with service, time fields (no ApiResponse wrapper)
echo "--- G05: /api/system/ping (public health ping) ---"
HTTP_CODE=$(curl -s -o /tmp/ping_resp.txt -w "%{http_code}" "$BASE_URL/api/system/ping")
PING_RESP=$(cat /tmp/ping_resp.txt)
if [ "$HTTP_CODE" = "200" ]; then
  PASS=$((PASS+1)); echo "  ✓ G05 HTTP 200 (HTTP $HTTP_CODE)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G05: expected HTTP 200, got HTTP $HTTP_CODE"); echo "  ✗ G05: expected HTTP 200, got HTTP $HTTP_CODE"
fi
# Verify raw map has 'service' and 'time' keys
HAS_SERVICE=$(echo "$PING_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if 'service' in d else 'no')" 2>/dev/null)
HAS_TIME=$(echo "$PING_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if 'time' in d else 'no')" 2>/dev/null)
if [ "$HAS_SERVICE" = "yes" ] && [ "$HAS_TIME" = "yes" ]; then
  PASS=$((PASS+1)); echo "  ✓ G05 ping response has service and time fields"
else
  FAIL=$((FAIL+1)); ERRORS+=("G05: ping response missing service/time fields"); echo "  ✗ G05: ping response missing service/time fields (service=$HAS_SERVICE, time=$HAS_TIME)"
fi

# G06: GET /admin/system/settings - returns RAW list (not wrapped in ApiResponse!)
echo "--- G06: /admin/system/settings (raw list, no ApiResponse wrapper) ---"
HTTP_CODE=$(curl -s -o /tmp/settings_list_resp.txt -w "%{http_code}" "$BASE_URL/admin/system/settings" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
SETTINGS_LIST=$(cat /tmp/settings_list_resp.txt)
if [ "$HTTP_CODE" = "200" ]; then
  PASS=$((PASS+1)); echo "  ✓ G06 HTTP 200 (HTTP $HTTP_CODE)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G06: expected HTTP 200, got HTTP $HTTP_CODE"); echo "  ✗ G06: expected HTTP 200, got HTTP $HTTP_CODE"
fi
# Verify response is a list (not ApiResponse with code field)
IS_LIST=$(echo "$SETTINGS_LIST" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, list) else 'no')" 2>/dev/null)
HAS_CODE=$(echo "$SETTINGS_LIST" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, dict) and 'code' in d else 'no')" 2>/dev/null)
if [ "$IS_LIST" = "yes" ] && [ "$HAS_CODE" = "no" ]; then
  PASS=$((PASS+1)); echo "  ✓ G06 response is raw list (not ApiResponse)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G06: response is not a raw list (is_list=$IS_LIST, has_code=$HAS_CODE)"); echo "  ✗ G06: response is not a raw list (is_list=$IS_LIST, has_code=$HAS_CODE)"
fi

# G07: PUT /admin/system/settings/{key} single key update - returns SettingResponse (raw, not ApiResponse)
echo "--- G07: PUT /admin/system/settings/test.e2e.key (single key update) ---"
HTTP_CODE=$(curl -s -o /tmp/put_single_resp.txt -w "%{http_code}" -X PUT "$BASE_URL/admin/system/settings/test.e2e.key" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"value":"e2e-test"}')
PUT_RESP=$(cat /tmp/put_single_resp.txt)
if [ "$HTTP_CODE" = "200" ]; then
  PASS=$((PASS+1)); echo "  ✓ G07 HTTP 200 (HTTP $HTTP_CODE)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G07: expected HTTP 200, got HTTP $HTTP_CODE"); echo "  ✗ G07: expected HTTP 200, got HTTP $HTTP_CODE"
fi
# Verify response is SettingResponse (raw object, not ApiResponse)
IS_OBJ=$(echo "$PUT_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, dict) and 'key' in d else 'no')" 2>/dev/null)
HAS_CODE=$(echo "$PUT_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, dict) and 'code' in d else 'no')" 2>/dev/null)
if [ "$IS_OBJ" = "yes" ] && [ "$HAS_CODE" = "no" ]; then
  PASS=$((PASS+1)); echo "  ✓ G07 response is raw SettingResponse (not ApiResponse)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G07: response is not a raw SettingResponse (is_obj=$IS_OBJ, has_code=$HAS_CODE)"); echo "  ✗ G07: response is not a raw SettingResponse (is_obj=$IS_OBJ, has_code=$HAS_CODE)"
fi
# Verify key and value
RESP_KEY=$(echo "$PUT_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('key',''))" 2>/dev/null)
RESP_VAL=$(echo "$PUT_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('value',''))" 2>/dev/null)
if [ "$RESP_KEY" = "test.e2e.key" ] && [ "$RESP_VAL" = "e2e-test" ]; then
  PASS=$((PASS+1)); echo "  ✓ G07 key=test.e2e.key, value=e2e-test"
else
  FAIL=$((FAIL+1)); ERRORS+=("G07: expected key=test.e2e.key value=e2e-test, got key=$RESP_KEY value=$RESP_VAL"); echo "  ✗ G07: expected key=test.e2e.key value=e2e-test, got key=$RESP_KEY value=$RESP_VAL"
fi

# G08: PUT /admin/system/settings batch update - returns List<SettingResponse> (raw, not ApiResponse)
echo "--- G08: PUT /admin/system/settings (batch update) ---"
HTTP_CODE=$(curl -s -o /tmp/put_batch_resp.txt -w "%{http_code}" -X PUT "$BASE_URL/admin/system/settings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"settings":{"test.e2e.k1":"v1","test.e2e.k2":"v2"}}')
BATCH_RESP=$(cat /tmp/put_batch_resp.txt)
if [ "$HTTP_CODE" = "200" ]; then
  PASS=$((PASS+1)); echo "  ✓ G08 HTTP 200 (HTTP $HTTP_CODE)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G08: expected HTTP 200, got HTTP $HTTP_CODE"); echo "  ✗ G08: expected HTTP 200, got HTTP $HTTP_CODE"
fi
# Verify response is a list (batch update returns List<SettingResponse>)
IS_LIST=$(echo "$BATCH_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, list) else 'no')" 2>/dev/null)
HAS_CODE=$(echo "$BATCH_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, dict) and 'code' in d else 'no')" 2>/dev/null)
if [ "$IS_LIST" = "yes" ] && [ "$HAS_CODE" = "no" ]; then
  PASS=$((PASS+1)); echo "  ✓ G08 response is raw list (not ApiResponse)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G08: response is not a raw list (is_list=$IS_LIST, has_code=$HAS_CODE)"); echo "  ✗ G08: response is not a raw list (is_list=$IS_LIST, has_code=$HAS_CODE)"
fi
# Verify batch update includes the new keys
HAS_K1=$(echo "$BATCH_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
found=False
for item in (d if isinstance(d, list) else []):
    if isinstance(item, dict) and item.get('key') == 'test.e2e.k1':
        found=True
        break
print('yes' if found else 'no')
" 2>/dev/null)
HAS_K2=$(echo "$BATCH_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
found=False
for item in (d if isinstance(d, list) else []):
    if isinstance(item, dict) and item.get('key') == 'test.e2e.k2':
        found=True
        break
print('yes' if found else 'no')
" 2>/dev/null)
if [ "$HAS_K1" = "yes" ] && [ "$HAS_K2" = "yes" ]; then
  PASS=$((PASS+1)); echo "  ✓ G08 batch update includes test.e2e.k1 and test.e2e.k2"
else
  FAIL=$((FAIL+1)); ERRORS+=("G08: batch update missing keys (k1=$HAS_K1, k2=$HAS_K2)"); echo "  ✗ G08: batch update missing keys (k1=$HAS_K1, k2=$HAS_K2)"
fi

# G09: GET /api/system/settings - public settings (raw map, not ApiResponse)
echo "--- G09: /api/system/settings (public settings) ---"
HTTP_CODE=$(curl -s -o /tmp/pub_settings_resp.txt -w "%{http_code}" "$BASE_URL/api/system/settings")
PUB_RESP=$(cat /tmp/pub_settings_resp.txt)
if [ "$HTTP_CODE" = "200" ]; then
  PASS=$((PASS+1)); echo "  ✓ G09 HTTP 200 (HTTP $HTTP_CODE)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G09: expected HTTP 200, got HTTP $HTTP_CODE"); echo "  ✗ G09: expected HTTP 200, got HTTP $HTTP_CODE"
fi
# Verify it's a map/dict (public settings), not an ApiResponse
IS_MAP=$(echo "$PUB_RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print('yes' if isinstance(d, dict) and 'code' not in d else 'no')" 2>/dev/null)
if [ "$IS_MAP" = "yes" ]; then
  PASS=$((PASS+1)); echo "  ✓ G09 response is raw map (public settings)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G09: response is not a raw map"); echo "  ✗ G09: response is not a raw map"
fi

# G10: Verify batch update actually applied (GET settings and check for keys)
echo "--- G10: 验证批量更新结果 ---"
HTTP_CODE=$(curl -s -o /tmp/verify_batch.txt -w "%{http_code}" "$BASE_URL/admin/system/settings" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
VERIFY_RESP=$(cat /tmp/verify_batch.txt)
HAS_K1=$(echo "$VERIFY_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
found=False
for item in (d if isinstance(d, list) else []):
    if isinstance(item, dict) and item.get('key') == 'test.e2e.k1':
        found=True
        break
print('yes' if found else 'no')
" 2>/dev/null)
HAS_K2=$(echo "$VERIFY_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
found=False
for item in (d if isinstance(d, list) else []):
    if isinstance(item, dict) and item.get('key') == 'test.e2e.k2':
        found=True
        break
print('yes' if found else 'no')
" 2>/dev/null)
if [ "$HAS_K1" = "yes" ] && [ "$HAS_K2" = "yes" ]; then
  PASS=$((PASS+1)); echo "  ✓ G10 batch update verified (k1 and k2 present)"
else
  FAIL=$((FAIL+1)); ERRORS+=("G10: batch update not verified (k1=$HAS_K1, k2=$HAS_K2)"); echo "  ✗ G10: batch update not verified (k1=$HAS_K1, k2=$HAS_K2)"
fi

summary "L2-G 系统设置"