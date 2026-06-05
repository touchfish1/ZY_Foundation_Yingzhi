#!/usr/bin/env bash
# tests/integration/L1-smoke.sh - 服务启动和健康检查
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L1: 冒烟测试 ==="

# 先登录获取 Token
echo "--- 登录 ---"
login
[ -n "$ADMIN_TOKEN" ] && echo "  ✓ Token acquired" || echo "  ✗ No token"

# 1. 后端服务健康检查
echo "--- 后端服务 ---"
assert_http_code "API Health" "200" "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8088/actuator/health)"
assert_http_code "Auth Health" "200" "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8082/admin/auth/login -X POST -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}')"
assert_http_code "System Health" "200" "$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/admin/system/monitor/stats)"
assert_http_code "Gateway Health" "200" "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/admin/auth/login -X POST -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}')"

# 2. 前端服务
echo "--- 前端服务 ---"
assert_http_code "Admin UI" "200" "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:5173/)"
assert_http_code "Web Site" "200" "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:3000/)"

summary "L1 冒烟测试"
