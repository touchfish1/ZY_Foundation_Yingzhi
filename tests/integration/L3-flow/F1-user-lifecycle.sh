#!/usr/bin/env bash
# tests/integration/L3-flow/F1-user-lifecycle.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== F1: 用户权限流程 ==="
login

E2E_UID="flow-e2e-$RANDOM"

echo "Step 1: 创建用户"
R=$(post_json "/admin/system/users" '{"username":"'$E2E_UID'","password":"Flow1234","nickname":"Flow E2E","email":"flow@e2e.com"}' "$ADMIN_TOKEN")
assert_code "F1.1 创建用户" "0" "$R"
USER_ID=$(get_field "$R" "id")
echo "  User ID: $USER_ID"

echo "Step 2: 创建角色并分配"
R=$(post_json "/admin/system/roles" '{"code":"'$E2E_UID'-role","name":"Flow E2E Role"}' "$ADMIN_TOKEN")
assert_code "F1.2 创建角色" "0" "$R"
ROLE_ID=$(get_field "$R" "id")
echo "  Role ID: $ROLE_ID"

if [ -n "$ROLE_ID" ] && [ -n "$USER_ID" ]; then
  R=$(put_json "/admin/system/users/$USER_ID/roles" "{\"roleIds\":[$ROLE_ID]}" "$ADMIN_TOKEN")
  assert_code "F1.3 分配角色" "0" "$R"
fi

summary "F1 用户权限流程"
