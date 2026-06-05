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

R=$(post_json "/admin/product/plan-groups" '{"code":"e2e-pg-'$RANDOM'","name":"E2E Group","description":"Test","status":"enabled","sortOrder":1}' "$ADMIN_TOKEN")
assert_code "D02 创建套餐组" "0" "$R"

echo "--- D03-D04: 套餐 ---"
R=$(get "/admin/product/plans" "$ADMIN_TOKEN")
assert_code "D03 套餐列表" "0" "$R"

echo "--- D05-D06: 价格 ---"
R=$(get "/admin/product/prices" "$ADMIN_TOKEN")
assert_code "D05 价格列表" "0" "$R"

echo "--- D08-D09: 公开产品 API ---"
R=$(get "/api/products/plan-groups/basic" "")
assert_code "D08 公开套餐组" "0" "$R"

R=$(get "/api/products/plans" "")
assert_code "D09 公开套餐列表" "0" "$R"

echo "--- D10-D11: DDD 产品 API ---"
R=$(get "/api/ddd/product/plan-groups" "$ADMIN_TOKEN")
assert_code "D10 DDD 套餐组" "0" "$R"

summary "L2-D 产品套餐"
