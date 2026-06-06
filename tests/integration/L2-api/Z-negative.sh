#!/usr/bin/env bash
# tests/integration/L2-api/Z-negative.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-Z: 负向测试（错误/边界输入）==="

# Helper: assert code != 0
assert_code_nz() {
  local label="$1" response="$2"
  local actual=$(get_code "$response")
  if [ "$actual" != "0" ]; then
    PASS=$((PASS+1))
    echo "  ✓ $label (code=$actual)"
  else
    FAIL=$((FAIL+1))
    local msg=$(echo "$response" | python3 -c "import sys,json;print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
    ERRORS+=("$label: expected non-zero code, got code=0")
    echo "  ✗ $label: expected non-zero code, got code=0 (msg: $msg)"
  fi
}

# Helper: get HTTP status code for a GET request (no token)
get_http_code_get() {
  local path="$1"
  curl -s -w "%{http_code}" -o /dev/null "$BASE_URL$path"
}

# Helper: get HTTP status code for a POST request (no token, no body)
get_http_code_post() {
  local path="$1"
  curl -s -w "%{http_code}" -o /dev/null -X POST "$BASE_URL$path"
}

# === Z01: POST /admin/auth/login with wrong password → code=500 ===
echo "--- Z01: 错误密码登录 ---"
R=$(post_json "/admin/auth/login" '{"username":"admin","password":"wrongpassword"}')
assert_code "Z01 错误密码" "500" "$R"

# === Z02: GET /admin/system/users without token → HTTP 401 ===
echo "--- Z02: 无 Token 访问受保护资源 ---"
HTTP_CODE=$(get_http_code_get "/admin/system/users")
assert_http_code "Z02 无Token" "401" "$HTTP_CODE"

# === Z03: GET /admin/system/users with invalid token → HTTP 401 ===
echo "--- Z03: 无效 Token 访问受保护资源 ---"
HTTP_CODE=$(curl -s -w "%{http_code}" -o /dev/null \
  -H "Authorization: Bearer invalid-token" \
  "$BASE_URL/admin/system/users")
assert_http_code "Z03 无效Token" "401" "$HTTP_CODE"

# === Z04: POST /api/orders with invalid planCode → code!=0 ===
echo "--- Z04: 无效 planCode 创建订单 ---"
R=$(post_json "/api/orders" '{"planCode":"NONEXISTENT-PLAN-CODE","quantity":1}')
assert_code_nz "Z04 无效planCode" "$R"

# === Z05: POST /api/orders with missing fields (body: {}) → code=400 or error ===
echo "--- Z05: 空请求体创建订单 ---"
R=$(post_json "/api/orders" '{}')
assert_code_nz "Z05 空请求体" "$R"

# === Z06: POST /api/payments/checkout with invalid orderNo → code!=0 ===
echo "--- Z06: 无效 orderNo 发起支付 ---"
R=$(post_json "/api/payments/checkout" '{"orderNo":"NONEXISTENT-ORDER-999","channel":"mock"}')
assert_code_nz "Z06 无效orderNo" "$R"

# === Z07: POST /api/payments/mock/NONEXISTENT-999/success → code!=0 ===
echo "--- Z07: 模拟支付不存在的支付单 ---"
R=$(post_json "/api/payments/mock/NONEXISTENT-999/success" '{}')
assert_code_nz "Z07 不存在支付单模拟" "$R"

# === Z08: GET /api/ddd/orders/NONEXISTENT-ORDER → code=404 (not found) ===
echo "--- Z08: 查询不存在的订单 ---"
R=$(get "/api/ddd/orders/NONEXISTENT-ORDER")
assert_code "Z08 不存在订单" "404" "$R"

summary "L2-Z 负向测试"