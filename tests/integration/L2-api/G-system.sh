#!/usr/bin/env bash
# tests/integration/L2-api/G-system.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-G: 系统设置测试 ==="
login

R=$(get "/admin/system/monitor/stats" "$ADMIN_TOKEN")
assert_code "G04 系统监控" "0" "$R"

summary "L2-G 系统设置"
