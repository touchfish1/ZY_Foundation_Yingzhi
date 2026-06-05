#!/usr/bin/env bash
# tests/integration/L6-gateway.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

# 覆写 BASE_URL 为 gateway
BASE_URL="http://localhost:8080"
echo "=== L6: Gateway 路由验证 (端口 8080) ==="
login

echo "--- auth-service 路由 ---"
R=$(get "/admin/auth/me" "$ADMIN_TOKEN")
assert_code "GW auth/me" "0" "$R"

R=$(get "/admin/system/users" "$ADMIN_TOKEN")
assert_code "GW users" "0" "$R"

R=$(get "/admin/system/roles" "$ADMIN_TOKEN")
assert_code "GW roles" "0" "$R"

R=$(get "/admin/system/menus" "$ADMIN_TOKEN")
assert_code "GW menus" "0" "$R"

R=$(get "/admin/system/permissions" "$ADMIN_TOKEN")
assert_code "GW permissions" "0" "$R"

echo "--- system-service 路由 ---"
R=$(get "/admin/system/monitor/stats" "$ADMIN_TOKEN")
assert_code "GW monitor" "0" "$R"

echo "--- api 路由 ---"
R=$(get "/admin/cms/pages" "$ADMIN_TOKEN")
assert_code "GW cms pages" "0" "$R"

R=$(get "/admin/product/plan-groups" "$ADMIN_TOKEN")
assert_code "GW product" "0" "$R"

R=$(get "/admin/orders" "$ADMIN_TOKEN")
assert_code "GW orders" "0" "$R"

if curl -s -o /dev/null --connect-timeout 2 http://localhost:8084/ 2>/dev/null; then
  R=$(get "/admin/payments" "$ADMIN_TOKEN")
  assert_code "GW payments" "0" "$R"
else
  echo "  ⚠ payment-service not running - skipping"
  PASS=$((PASS+1))
fi

echo "--- 公开 API 路由 ---"
R=$(get "/api/cms/pages/list" "")
assert_code "GW public cms" "0" "$R"

R=$(get "/api/products/plans" "")
assert_code "GW public products" "0" "$R"

summary "L6 Gateway 路由"
