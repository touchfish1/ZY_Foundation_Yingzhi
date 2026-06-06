#!/usr/bin/env bash
# Project ZHANGYUAN — CI Smoke Tests
# Tests that all services start up and key API endpoints respond correctly.
# Usage: bash tests/integration/smoke_test.sh [BASE_URL]
#   BASE_URL defaults to http://localhost:8080 (Gateway)

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0

green() { printf "\033[32m✓ %s\033[0m\n" "$1"; }
red()   { printf "\033[31m✗ %s\033[0m\n" "$1"; }

assert_status() {
  local desc="$1" method="$2" path="$3" expected="$4" auth="$5"
  local code
  if [ -n "$auth" ]; then
    code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" \
      -H "Authorization: Bearer $auth" \
      "${BASE_URL}${path}" 2>/dev/null || echo "000")
  else
    code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" \
      "${BASE_URL}${path}" 2>/dev/null || echo "000")
  fi
  if [ "$code" = "$expected" ]; then
    green "$desc"
    PASS=$((PASS+1))
  else
    red "$desc (expected $expected, got $code)"
    FAIL=$((FAIL+1))
  fi
}

echo "========================================"
echo "  Project ZHANGYUAN — Smoke Tests"
echo "  Base URL: $BASE_URL"
echo "========================================"
echo ""

# ── 1. Health / Ping ──────────────────────────────
assert_status "Ping system-service"           GET  "/api/system/ping"  "200" ""

# ── 2. Public API endpoints ─────────────────────
assert_status "List plans (public)"            GET  "/api/products/plans"  "200" ""
assert_status "List CMS pages (public)"         GET  "/api/cms/pages/list?type=blog"  "200" ""
assert_status "Get system settings (public)"    GET  "/api/system/settings"  "200" ""

# ── 3. Admin auth ────────────────────────────────
ADMIN_TOKEN=$(curl -s -X POST "${BASE_URL}/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  2>/dev/null | grep -o '"tokenValue":"[^"]*"' | cut -d'"' -f4 || echo "")

if [ -z "$ADMIN_TOKEN" ]; then
  red "Admin login failed — cannot proceed with authenticated tests"
  FAIL=$((FAIL+1))
else
  green "Admin login obtained token"

  assert_status "Get admin profile"             GET  "/admin/auth/me"  "200" "$ADMIN_TOKEN"
  assert_status "List admin users"              GET  "/admin/system/users?page=1&pageSize=10"  "200" "$ADMIN_TOKEN"
  assert_status "List admin roles"              GET  "/admin/system/roles?page=1&pageSize=10"  "200" "$ADMIN_TOKEN"
  assert_status "List permissions"              GET  "/admin/system/permissions?page=1&pageSize=10"  "200" "$ADMIN_TOKEN"
  assert_status "List menus"                    GET  "/admin/system/menus"  "200" "$ADMIN_TOKEN"
  assert_status "CMS pages (admin)"             GET  "/admin/cms/pages"  "200" "$ADMIN_TOKEN"
  assert_status "List plan groups (admin)"      GET  "/admin/product/plan-groups"  "200" "$ADMIN_TOKEN"
  assert_status "List orders (admin)"           GET  "/admin/orders"  "200" "$ADMIN_TOKEN"
  assert_status "List subscriptions (admin)"    GET  "/admin/subscriptions"  "200" "$ADMIN_TOKEN"
  assert_status "List payments (admin)"         GET  "/admin/payments"  "200" "$ADMIN_TOKEN"
  assert_status "System settings (admin)"       GET  "/admin/system/settings"  "200" "$ADMIN_TOKEN"
  assert_status "System monitor"                GET  "/admin/system/monitor/stats"  "200" "$ADMIN_TOKEN"
fi

# ── 4. SaaS user flow ───────────────────────────
SAAS_TOKEN=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"smoketest@test.com","password":"Test1234","nickname":"Smoke Tester"}' \
  2>/dev/null | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || echo "")

if [ -z "$SAAS_TOKEN" ]; then
  # Try login instead (user may already exist)
  SAAS_TOKEN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"smoketest@test.com","password":"Test1234"}' \
    2>/dev/null | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || echo "")
fi

if [ -z "$SAAS_TOKEN" ]; then
  red "SaaS user login/register failed"
  FAIL=$((FAIL+1))
else
  green "SaaS user authenticated"

  assert_status "Get SaaS profile"              GET  "/api/auth/profile"  "200" "$SAAS_TOKEN"
  assert_status "Get balance"                   GET  "/api/balance/1"  "200" "$SAAS_TOKEN"
fi

# ── 5. Auth error cases ─────────────────────────
assert_status "401: no token on admin endpoint" GET  "/admin/system/users"  "401" ""
assert_status "404: unknown order"             GET  "/api/orders/NONEXISTENT"  "404" "$SAAS_TOKEN"

# ── Summary ──────────────────────────────────────
echo ""
echo "========================================"
echo "  Results: $PASS passed, $FAIL failed"
echo "========================================"
exit $FAIL
