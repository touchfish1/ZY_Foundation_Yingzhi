#!/usr/bin/env bash
# tests/integration/L2-api/D-product.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-D: 产品套餐测试 ==="
login

echo "--- D01-D02: 套餐组 ---"
R=$(get "/admin/product/plan-groups" "$ADMIN_TOKEN")
assert_code "D01 套餐组列表" "0" "$R"

PG_CODE="e2e-pg-$RANDOM"
R=$(post_json "/admin/product/plan-groups" "{\"code\":\"$PG_CODE\",\"name\":\"E2E Group\",\"description\":\"Test\",\"status\":\"enabled\",\"sortOrder\":1}" "$ADMIN_TOKEN")
assert_code "D02 创建套餐组" "0" "$R"
# Extract groupId from list since create response returns id:null
R=$(get "/admin/product/plan-groups" "$ADMIN_TOKEN")
GROUP_ID=$(echo "$R" | python3 -c "import sys,json;d=json.load(sys.stdin);items=d.get('data',[]);print(next((str(p['id']) for p in items if p['code']=='${PG_CODE}'),''))" 2>/dev/null)

echo "--- D03-D08: 套餐与定价 ---"
R=$(get "/admin/product/plans" "$ADMIN_TOKEN")
assert_code "D03 套餐列表" "0" "$R"

PLAN_CODE="e2e-plan-$RANDOM"
R=$(post_json "/admin/product/plans" "{\"groupId\":$GROUP_ID,\"code\":\"$PLAN_CODE\",\"name\":\"E2E Plan\",\"description\":\"Test\",\"sortOrder\":1}" "$ADMIN_TOKEN")
assert_code "D06 创建套餐" "0" "$R"
# Extract planId from list since create response returns id:null
R=$(get "/admin/product/plans" "$ADMIN_TOKEN")
PLAN_ID=$(echo "$R" | python3 -c "import sys,json;d=json.load(sys.stdin);items=d.get('data',[]);print(next((str(p['id']) for p in items if p['code']=='${PLAN_CODE}'),''))" 2>/dev/null)

R=$(post_json "/admin/product/prices" "{\"planId\":$PLAN_ID,\"currency\":\"CNY\",\"billingCycle\":\"monthly\",\"amount\":99.00,\"originalAmount\":99.00}" "$ADMIN_TOKEN")
assert_code "D07 创建价格" "0" "$R"

R=$(post_json "/admin/product/features" "{\"planId\":$PLAN_ID,\"featureName\":\"E2E Feature\",\"featureValue\":\"1000\",\"included\":true,\"sortOrder\":1}" "$ADMIN_TOKEN")
assert_code "D08 创建功能项" "0" "$R"

echo "--- D09-D11: 价格与公开 API ---"
R=$(get "/admin/product/prices" "$ADMIN_TOKEN")
assert_code "D09 价格列表" "0" "$R"

R=$(get "/api/products/plan-groups/basic" "")
assert_code "D10 公开套餐组" "0" "$R"

R=$(get "/api/products/plans" "")
assert_code "D11 公开套餐列表" "0" "$R"

summary "L2-D 产品套餐"