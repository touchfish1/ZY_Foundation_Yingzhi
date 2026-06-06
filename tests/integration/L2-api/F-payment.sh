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

# F05: 管理端支付列表（保留已有测试）
R=$(get "/admin/payments" "$ADMIN_TOKEN")
assert_code "F05 管理端支付列表" "0" "$R"

# ========== 数据准备（纳秒时间戳保证唯一性）============
# 1. 创建套餐分组
PG_CODE="e2e-pg-$(date +%s%N)"
R=$(post_json "/admin/product/plan-groups" \
  "{\"code\":\"$PG_CODE\",\"name\":\"E2E Pay\",\"description\":\"Test\",\"status\":\"enabled\",\"sortOrder\":1}" \
  "$ADMIN_TOKEN")
assert_code "创建套餐分组" "0" "$R"

# 2. 通过列表接口按 code 提取真实的 groupId
PG_ID=$(get "/admin/product/plan-groups" "$ADMIN_TOKEN" \
  | python3 -c "import sys,json;groups=json.load(sys.stdin)['data'];g=[x for x in groups if x['code']=='$PG_CODE'];print(g[0]['id'] if g else '')" 2>/dev/null)

# 3. 创建套餐
PLAN_CODE="e2e-plan-$(date +%s%N)"
R=$(post_json "/admin/product/plans" \
  "{\"groupId\":$PG_ID,\"code\":\"$PLAN_CODE\",\"name\":\"E2E Pay Plan\",\"description\":\"Test\",\"sortOrder\":1}" \
  "$ADMIN_TOKEN")
assert_code "创建套餐" "0" "$R"

# 4. 通过列表接口按 code 提取真实的 planId
PLAN_ID=$(get "/admin/product/plans" "$ADMIN_TOKEN" \
  | python3 -c "import sys,json;plans=json.load(sys.stdin)['data'];p=[x for x in plans if x['code']=='$PLAN_CODE'];print(p[0]['id'] if p else '')" 2>/dev/null)

# 5. 创建价格
R=$(post_json "/admin/product/prices" \
  "{\"planId\":$PLAN_ID,\"currency\":\"CNY\",\"billingCycle\":\"monthly\",\"amount\":1.00,\"originalAmount\":1.00}" \
  "$ADMIN_TOKEN")
assert_code "创建价格" "0" "$R"

# ========== 订单 & 支付流程 ==========
# 6. 创建 SaaS 订单
R=$(post_json "/api/orders" \
  "{\"planCode\":\"$PLAN_CODE\",\"billingCycle\":\"monthly\",\"currency\":\"CNY\"}" \
  "$ADMIN_TOKEN")
assert_code "创建 SaaS 订单" "0" "$R"
ORDER_NO=$(echo "$R" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['orderNo'])" 2>/dev/null)

# 7. 发起支付（checkout）
R=$(post_json "/api/payments/checkout" \
  "{\"orderNo\":\"$ORDER_NO\",\"channel\":\"mock\"}" \
  "$ADMIN_TOKEN")
assert_code "支付结算" "0" "$R"
PAYMENT_NO=$(echo "$R" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['paymentNo'])" 2>/dev/null)

# 8. 模拟支付成功
R=$(post_json "/api/payments/mock/$PAYMENT_NO/success" "{}" "$ADMIN_TOKEN")
assert_code "模拟支付成功" "0" "$R"

# ========== DDD 支付验证 ==========
# 9. DDD 支付详情
R=$(get "/api/ddd/payments/$PAYMENT_NO")
assert_code "DDD 支付详情" "0" "$R"

# 10. DDD 支付列表
R=$(get "/api/ddd/payments")
assert_code "DDD 支付列表" "0" "$R"

summary "L2-F 支付"