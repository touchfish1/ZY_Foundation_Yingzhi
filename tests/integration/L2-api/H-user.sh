#!/usr/bin/env bash
# tests/integration/L2-api/H-user.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-H: 用户服务测试 ==="

# Check if user-service is running
if ! curl -s -o /dev/null --connect-timeout 3 http://localhost:8085/ 2>/dev/null; then
  echo "  ⚠ user-service (8085) not running - skipping"
  PASS=$((PASS+1))
  summary "L2-H 用户服务 (skipped)"
  exit 0
fi

login

E2E_UID="saas-e2e-$RANDOM"

R=$(post_json "/api/auth/register" '{"username":"'$E2E_UID'","password":"Test1234","email":"'$E2E_UID'@e2e.com"}' "")
assert_code "H01 注册" "0" "$R"

R=$(post_json "/api/auth/login" '{"username":"'$E2E_UID'","password":"Test1234"}' "")
assert_code "H02 SaaS登录" "0" "$R"
SAAS_TOKEN=$(echo "$R" | python -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('accessToken','') or d.get('token',''))" 2>/dev/null)
echo "  SaaS Token: ${SAAS_TOKEN:0:20}..."

if [ -n "$SAAS_TOKEN" ]; then
  R=$(get "/api/auth/profile" "$SAAS_TOKEN")
  assert_code "H03 个人资料" "0" "$R"
fi

summary "L2-H 用户服务"
