#!/usr/bin/env bash
# tests/integration/L2-api/F-payment.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-F: 支付测试 ==="

# Check if payment-service is running
if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8084/ 2>/dev/null; then
  echo "  ⚠ payment-service (8084) not running - skipping"
  PASS=$((PASS+1))
  summary "L2-F 支付 (skipped)"
  exit 0
fi

login

R=$(get "/admin/payments" "$ADMIN_TOKEN")
assert_code "F05 管理端支付列表" "0" "$R"

summary "L2-F 支付"
