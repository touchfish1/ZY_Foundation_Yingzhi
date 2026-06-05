#!/usr/bin/env bash
# tests/integration/L5-frontend.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L5: 前端冒烟测试 ==="

WEB_PAGES=(
  "/"
  "/pricing"
  "/docs"
  "/about"
  "/blog"
)

echo "--- Web Frontend ---"
for path in "${WEB_PAGES[@]}"; do
  http_code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:3000$path" --connect-timeout 5)
  assert_http_code "Web $path" "200" "$http_code"
done

# 检查 Admin 前端是否可访问（SPA, 会返回 index.html）
echo "--- Admin UI ---"
http_code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:5173/" --connect-timeout 5)
assert_http_code "Admin /" "200" "$http_code"

http_code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:5173/login" --connect-timeout 5)
assert_http_code "Admin /login" "200" "$http_code"

summary "L5 前端冒烟"
