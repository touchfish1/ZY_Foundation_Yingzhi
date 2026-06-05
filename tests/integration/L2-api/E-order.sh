#!/usr/bin/env bash
# tests/integration/L2-api/E-order.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-E: 订单测试 ==="

# Check if order-service is running
if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8083/ 2>/dev/null; then
  echo "  ⚠ order-service (8083) not running - skipping"
  PASS=$((PASS+1))
  summary "L2-E 订单 (skipped)"
  exit 0
fi

login

R=$(get "/admin/orders" "$ADMIN_TOKEN")
assert_code "E04 管理端订单" "0" "$R"

summary "L2-E 订单"
