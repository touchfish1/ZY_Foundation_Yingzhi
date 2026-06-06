#!/usr/bin/env bash
# tests/integration/L2-api/A-auth.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-A: 认证与权限测试 ==="
login

# A01-A03: 登录
echo "--- A01-A03: 登录 ---"
R=$(post_json "/admin/auth/login" '{"username":"admin","password":"admin123"}')
assert_code "A01 正确登录" "0" "$R"

R=$(post_json "/admin/auth/login" '{"username":"admin","password":"wrong"}')
assert_code "A02 错误密码" "500" "$R"

R=$(post_json "/admin/auth/login" '{}')
assert_code "A03 空请求体" "400" "$R"

# A04-A06: 当前用户
echo "--- A04-A06: 当前用户 ---"
R=$(get "/admin/auth/me" "$ADMIN_TOKEN")
assert_code "A04 带 Token 获取用户" "0" "$R"
echo "$R" | python3 -c "import sys,json;d=json.load(sys.stdin);print('  User:', d.get('data',{}).get('username','?'))" 2>/dev/null || true

R=$(get "/admin/auth/me" "")
assert_code "A05 无 Token" "401" "$R"

R=$(get "/admin/auth/me" "invalid-token")
assert_code "A06 无效 Token" "401" "$R"

# A07: 菜单
echo "--- A07: 用户菜单 ---"
R=$(get "/admin/auth/menus" "$ADMIN_TOKEN")
assert_code "A07 获取菜单" "0" "$R"

# A08-A10: 用户管理
echo "--- A08-A10: 用户管理 ---"
R=$(get "/admin/system/users" "$ADMIN_TOKEN")
assert_code "A08 用户列表" "0" "$R"
assert_contains "A08 有数据" "data.items" "$R"

R=$(post_json "/admin/system/users" '{"username":"test-e2e-'$RANDOM'","password":"Test1234","nickname":"E2E Test","email":"e2e@test.com"}' "$ADMIN_TOKEN")
assert_code "A09 创建用户" "0" "$R"
USER_ID=$(get_field "$R" "id")

if [ -n "$USER_ID" ]; then
  R=$(put_json "/admin/system/users/$USER_ID" '{"nickname":"E2E Updated"}' "$ADMIN_TOKEN")
  assert_code "A10 更新用户" "0" "$R"
fi

# A11-A13: 角色管理
echo "--- A11-A13: 角色管理 ---"
R=$(get "/admin/system/roles" "$ADMIN_TOKEN")
assert_code "A11 角色列表" "0" "$R"
assert_contains "A11 有数据" "data.items" "$R"

R=$(post_json "/admin/system/roles" '{"code":"e2e-role-'$RANDOM'","name":"E2E Test Role"}' "$ADMIN_TOKEN")
assert_code "A12 创建角色" "0" "$R"
ROLE_ID=$(get_field "$R" "id")

if [ -n "$ROLE_ID" ]; then
  R=$(put_json "/admin/system/roles/$ROLE_ID/permissions" '{"permissionIds":[1,2,3]}' "$ADMIN_TOKEN")
  assert_code "A13 分配权限" "0" "$R"
fi

# A14-A15: 权限
echo "--- A14-A15: 权限 ---"
R=$(get "/admin/system/permissions" "$ADMIN_TOKEN")
assert_code "A14 权限列表" "0" "$R"

R=$(get "/admin/system/permissions/modules" "$ADMIN_TOKEN")
assert_code "A15 权限模块" "0" "$R"

# A16-A20: 菜单管理
echo "--- A16-A20: 菜单管理 ---"
R=$(get "/admin/system/menus" "$ADMIN_TOKEN")
assert_code "A16 菜单树" "0" "$R"

R=$(post_json "/admin/system/menus" '{"name":"E2E Menu","path":"/e2e","icon":"Dashboard","menuType":"page","sortOrder":99}' "$ADMIN_TOKEN")
assert_code "A17 创建菜单" "0" "$R"
MENU_ID=$(get_field "$R" "id")

if [ -n "$MENU_ID" ]; then
  R=$(put_json "/admin/system/menus/$MENU_ID" '{"name":"E2E Menu Updated","path":"/e2e","menuType":"page","sortOrder":99}' "$ADMIN_TOKEN")
  assert_code "A18 更新菜单" "0" "$R"

  R=$(delete "/admin/system/menus/$MENU_ID" "$ADMIN_TOKEN")
  assert_code "A19 删除菜单" "0" "$R"
fi

summary "L2-A 认证与权限"
