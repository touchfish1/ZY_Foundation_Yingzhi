#!/usr/bin/env bash
# tests/integration/L2-api/C-asset.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-C: 资产管理测试 ==="
login

R=$(get "/admin/assets/files" "$ADMIN_TOKEN")
assert_code "C01 文件列表" "0" "$R"

R=$(get "/api/ddd/assets" "$ADMIN_TOKEN")
assert_code "C03 DDD 资产列表" "0" "$R"

summary "L2-C 资产管理"
