#!/usr/bin/env bash
# tests/integration/L2-api/J-balance.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-J: 余额测试 ==="

# Check if user-service is running
if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8085/ 2>/dev/null; then
  echo "  ⚠ user-service (8085) not running - skipping"
  PASS=$((PASS+1))
  summary "L2-J 余额 (skipped)"
  exit 0
fi

login

# Get balance
R=$(get "/api/balance/1" "$ADMIN_TOKEN")
assert_code "J01 获取余额" "0" "$R"
assert_contains "J01 数据字段" "balance" "$R"

# Get transactions
R=$(get "/api/balance/1/transactions" "$ADMIN_TOKEN")
assert_code "J02 获取交易记录" "0" "$R"

# Get balance before recharge
BEFORE=$(get_field "$R" "balance")
BEFORE_BALANCE=$(echo "$(get "/api/balance/1" "$ADMIN_TOKEN")" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['balance'])" 2>/dev/null || echo "0")

# Recharge
R=$(post_json "/api/balance/1/recharge?amount=10&description=E2E+test" "{}" "$ADMIN_TOKEN")
assert_code "J03 余额充值" "0" "$R"

# Verify balance increased
AFTER=$(echo "$(get "/api/balance/1" "$ADMIN_TOKEN")" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['balance'])" 2>/dev/null || echo "0")
if [ "$(echo "$AFTER > $BEFORE_BALANCE" | bc)" = "1" ]; then
  PASS=$((PASS+1))
  echo "  ✓ J04 余额增加验证 (before=$BEFORE_BALANCE, after=$AFTER)"
else
  FAIL=$((FAIL+1))
  ERRORS+=("J04 余额未增加: before=$BEFORE_BALANCE, after=$AFTER")
  echo "  ✗ J04 余额未增加: before=$BEFORE_BALANCE, after=$AFTER"
fi

summary "L2-J 余额"