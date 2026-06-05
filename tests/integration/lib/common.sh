#!/usr/bin/env bash
# tests/integration/lib/common.sh
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_TOKEN=""
PASS=0
FAIL=0
ERRORS=()

login() {
  local resp=$(curl -s "$BASE_URL/admin/auth/login" \
    -X POST -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
  ADMIN_TOKEN=$(echo "$resp" | python -c "import sys,json;print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null)
  if [ -z "$ADMIN_TOKEN" ]; then
    echo "FATAL: Cannot obtain admin token (response: $resp)"
    exit 1
  fi
  echo "  [OK] Admin token acquired"
}

post_json() {
  local path="$1" data="$2" token="${3:-}"
  if [ -n "$token" ]; then
    curl -s "$BASE_URL$path" -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d "$data"
  else
    curl -s "$BASE_URL$path" -X POST -H "Content-Type: application/json" -d "$data"
  fi
}

get() {
  local path="$1" token="${2:-}"
  if [ -n "$token" ]; then
    curl -s "$BASE_URL$path" -H "Authorization: Bearer $token"
  else
    curl -s "$BASE_URL$path"
  fi
}

put_json() {
  local path="$1" data="$2" token="${3:-}"
  if [ -n "$token" ]; then
    curl -s "$BASE_URL$path" -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d "$data"
  else
    curl -s "$BASE_URL$path" -X PUT -H "Content-Type: application/json" -d "$data"
  fi
}

delete() {
  local path="$1" token="$2"
  curl -s "$BASE_URL$path" -X DELETE -H "Authorization: Bearer $token"
}

get_code() {
  echo "$1" | python -c "import sys,json;print(json.load(sys.stdin).get('code', -1))" 2>/dev/null
}

get_field() {
  local json="$1" field="$2"
  echo "$json" | python -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('$field','') if isinstance(d.get('data'), dict) else d.get('$field',''))" 2>/dev/null
}

assert_code() {
  local label="$1" expected="$2" response="$3"
  local actual=$(get_code "$response")
  if [ "$actual" = "$expected" ]; then
    PASS=$((PASS+1))
    echo "  ✓ $label (code=$actual)"
  else
    FAIL=$((FAIL+1))
    local msg=$(echo "$response" | python -c "import sys,json;print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
    ERRORS+=("$label: expected code=$expected, got code=$actual, msg=$msg")
    echo "  ✗ $label: expected $expected, got $actual (msg: $msg)"
  fi
}

assert_http_code() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    PASS=$((PASS+1))
    echo "  ✓ $label (HTTP $actual)"
  else
    FAIL=$((FAIL+1))
    ERRORS+=("$label: expected HTTP $expected, got HTTP $actual")
    echo "  ✗ $label: expected HTTP $expected, got HTTP $actual"
  fi
}

assert_contains() {
  local label="$1" field="$2" response="$3"
  local val=$(echo "$response" | python -c "
import sys,json
d=json.load(sys.stdin)
data = d.get('data', '')
if isinstance(data, list) and len(data) > 0:
    print('present')
elif isinstance(data, dict):
    keys='$field'.split('.')[1:]
    v=data
    for k in keys:
        v=v.get(k,'') if isinstance(v, dict) else ''
    print(v if v else 'missing')
else:
    print('present' if data else 'missing')
" 2>/dev/null)
  if [ "$val" != "missing" ] && [ -n "$val" ]; then
    PASS=$((PASS+1))
    echo "  ✓ $label ($field present)"
  else
    FAIL=$((FAIL+1))
    ERRORS+=("$label: missing field $field")
    echo "  ✗ $label: missing $field"
  fi
}

summary() {
  local suite_name="${1:-Test Suite}"
  echo ""
  echo "=============================="
  echo "  $suite_name"
  echo "  PASSED: $PASS"
  echo "  FAILED: $FAIL"
  echo "=============================="
  if [ ${#ERRORS[@]} -gt 0 ]; then
    echo "  Errors:"
    for e in "${ERRORS[@]}"; do echo "    - $e"; done
  fi
  [ $FAIL -eq 0 ] && return 0 || return 1
}
