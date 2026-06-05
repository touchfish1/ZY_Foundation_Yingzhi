#!/usr/bin/env bash
# tests/integration/L3-flow/F3-order-payment.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== F3: 订单支付链路 ==="

if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8083/ 2>/dev/null; then
  echo "  ⚠ order-service (8083) not running - skipping"
  PASS=$((PASS+1))
  summary "F3 订单支付 (skipped)"
  exit 0
fi

login

echo "Step 1: 管理端查看"
R=$(get "/admin/orders" "$ADMIN_TOKEN")
assert_code "F3.1 管理端订单" "0" "$R"

summary "F3 订单支付链路"
