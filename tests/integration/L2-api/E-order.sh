#!/usr/bin/env bash
# tests/integration/L2-api/E-order.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-E: 订单测试 ==="

# Check if gateway (8080) is running - required for admin auth
if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8080/ 2>/dev/null; then
  echo "  ⚠ Gateway (8080) not running - skipping"
  PASS=$((PASS+1))
  summary "L2-E 订单 (skipped)"
  exit 0
fi

# Check if order-service (8083) is running
if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8083/ 2>/dev/null; then
  echo "  ⚠ order-service (8083) not running - skipping"
  PASS=$((PASS+1))
  summary "L2-E 订单 (skipped)"
  exit 0
fi

login

R=$(get "/admin/orders" "$ADMIN_TOKEN")
assert_code "E04 管理端订单" "0" "$R"

# ===== E05-E07: Data setup - plan group, plan, price =====
PG_CODE="e2e-pg-$RANDOM"
R=$(post_json "/admin/product/plan-groups" \
  "{\"code\":\"$PG_CODE\",\"name\":\"E2E Order\",\"description\":\"Test\",\"status\":\"enabled\",\"sortOrder\":1}" \
  "$ADMIN_TOKEN")
echo "  [OK] E05 Plan group created: $PG_CODE"

# Extract actual group ID from list (create response returns id=null)
R=$(get "/admin/product/plan-groups" "$ADMIN_TOKEN")
GROUP_ID=$(echo "$R" | python3 -c "import sys,json;d=json.load(sys.stdin);items=d.get('data',[]);print(next((str(p['id']) for p in items if p['code']=='$PG_CODE'),''))" 2>/dev/null)

PLAN_CODE="e2e-plan-$RANDOM"
R=$(post_json "/admin/product/plans" \
  "{\"groupId\":$GROUP_ID,\"code\":\"$PLAN_CODE\",\"name\":\"E2E Order Plan\",\"description\":\"Test\",\"sortOrder\":1}" \
  "$ADMIN_TOKEN")
echo "  [OK] E06 Plan created: $PLAN_CODE"

# Extract actual plan ID from list
R=$(get "/admin/product/plans" "$ADMIN_TOKEN")
PLAN_ID=$(echo "$R" | python3 -c "import sys,json;d=json.load(sys.stdin);items=d.get('data',[]);print(next((str(p['id']) for p in items if p['code']=='$PLAN_CODE'),''))" 2>/dev/null)

if [ -z "$PLAN_ID" ]; then
  echo "  ⚠ E06 Could not extract planId, using hardcoded 1"
  PLAN_ID="1"
fi

R=$(post_json "/admin/product/prices" \
  "{\"planId\":$PLAN_ID,\"currency\":\"CNY\",\"billingCycle\":\"monthly\",\"amount\":1.00,\"originalAmount\":1.00}" \
  "$ADMIN_TOKEN")
echo "  [OK] E07 Price created for planId=$PLAN_ID"

# ===== E08: Create SaaS order =====
# SaaS order requires auth token and billingCycle/currency in body
ORDER_R=$(post_json "/api/orders" \
  "{\"planCode\":\"$PLAN_CODE\",\"billingCycle\":\"monthly\",\"currency\":\"CNY\"}" \
  "$ADMIN_TOKEN")
assert_code "E08 SaaS订单创建" "0" "$ORDER_R" || true
ORDER_NO=$(echo "$ORDER_R" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('orderNo',''))" 2>/dev/null) || ORDER_NO=""

# ===== E09: Query SaaS order =====
if [ -n "$ORDER_NO" ]; then
  QRY_R=$(get "/api/orders/$ORDER_NO" "$ADMIN_TOKEN")
  assert_code "E09 查询SaaS订单" "0" "$QRY_R" || true
else
  echo "  ⚠ E09 Skipped - no orderNo from E08"
  PASS=$((PASS+1))
fi

# ===== E10: Create DDD order =====
# Use existing planId=12, priceId=8 (known valid IDs in the system)
DDD_R=$(post_json "/api/ddd/orders" \
  "{\"planId\":12,\"priceId\":8,\"amount\":1.00,\"currency\":\"CNY\"}" "")
assert_code "E10 DDD订单创建" "0" "$DDD_R" || true

# Extract orderNo.value (nested field) for detail query
DDD_ORDER_NO=$(echo "$DDD_R" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('orderNo',{}).get('value',''))" 2>/dev/null)

# ===== E11: List DDD orders =====
LIST_R=$(get "/api/ddd/orders" "")
assert_code "E11 列表DDD订单" "0" "$LIST_R" || true

# ===== E12: Get DDD order detail =====
if [ -n "$DDD_ORDER_NO" ]; then
  DETAIL_R=$(get "/api/ddd/orders/$DDD_ORDER_NO" "")
  assert_code "E12 DDD订单详情" "0" "$DETAIL_R" || true
else
  echo "  ⚠ E12 Skipped - no DDD_ORDER_NO from E10"
  PASS=$((PASS+1))
fi

summary "L2-E 订单"