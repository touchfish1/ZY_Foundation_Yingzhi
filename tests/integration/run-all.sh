#!/usr/bin/env bash
# tests/integration/run-all.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$SCRIPT_DIR/report-$TIMESTAMP.log"
ALL_PASS=true

echo "========================================="
echo "  全栈集成测试 - $(date)"
echo "========================================="
echo ""

run_suite() {
  local label="$1" script="$2"
  echo "---------------------------------------"
  echo "  运行: $label"
  echo "---------------------------------------"
  if bash "$script" 2>&1 | tee -a "$REPORT_FILE"; then
    echo "  ✅ $label - 通过"
  else
    echo "  ❌ $label - 失败"
    ALL_PASS=false
  fi
  echo ""
}

# L1: 冒烟
run_suite "L1 冒烟测试" "$SCRIPT_DIR/L1-smoke.sh"

if [ "$ALL_PASS" = true ]; then
  # L2: API 模块
  run_suite "L2-A 认证" "$SCRIPT_DIR/L2-api/A-auth.sh"
  run_suite "L2-B CMS" "$SCRIPT_DIR/L2-api/B-cms.sh"
  run_suite "L2-C 资产" "$SCRIPT_DIR/L2-api/C-asset.sh"
  run_suite "L2-D 产品" "$SCRIPT_DIR/L2-api/D-product.sh"
  run_suite "L2-E 订单" "$SCRIPT_DIR/L2-api/E-order.sh"
  run_suite "L2-F 支付" "$SCRIPT_DIR/L2-api/F-payment.sh"
  run_suite "L2-G 系统" "$SCRIPT_DIR/L2-api/G-system.sh"
  run_suite "L2-H 用户" "$SCRIPT_DIR/L2-api/H-user.sh"

  # L3: 业务流程
  run_suite "F1 用户权限" "$SCRIPT_DIR/L3-flow/F1-user-lifecycle.sh"
  run_suite "F2 CMS发布" "$SCRIPT_DIR/L3-flow/F2-cms-publish.sh"
  run_suite "F3 订单支付" "$SCRIPT_DIR/L3-flow/F3-order-payment.sh"

  # L5: 前端
  run_suite "L5 前端冒烟" "$SCRIPT_DIR/L5-frontend.sh"

  # L6: Gateway
  run_suite "L6 Gateway" "$SCRIPT_DIR/L6-gateway.sh"
fi

echo "========================================="
if [ "$ALL_PASS" = true ]; then
  echo "  全部测试通过"
else
  echo "  存在失败的测试 - 请检查: $REPORT_FILE"
  grep "✗" "$REPORT_FILE" 2>/dev/null || true
fi
echo "========================================="

[ "$ALL_PASS" = true ] && exit 0 || exit 1
