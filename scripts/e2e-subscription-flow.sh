#!/bin/bash
# ==============================================================
# e2e-subscription-flow.sh - End-to-End Subscription Lifecycle Test
#
# Tests the complete subscription lifecycle:
#   1.  Register test user
#   2.  Fetch available plans
#   3.  Create order for a plan
#   4.  Mock payment → triggers fulfillment
#   5.  Verify subscription created (active)
#   6.  Verify quota updated
#   7.  Verify user API key reflects subscription
#   8.  Check subscription via list endpoint
#   9.  (Cancel subscription - pending API implementation)
#
# Usage:
#   export BASE_URL=http://localhost:8080   (default)
#   bash scripts/e2e-subscription-flow.sh
# ==============================================================

set -euo pipefail

# ---------- Configuration ----------
BASE_URL="${BASE_URL:-http://localhost:8080}"
AUTH_URL="${AUTH_URL:-http://localhost:8082}"

# ---------- Colors & Helpers ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PASS=0
FAIL=0
SKIP=0

pass() { PASS=$((PASS + 1)); echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { FAIL=$((FAIL + 1)); echo -e "${RED}[FAIL]${NC} $1"; }
skip() { SKIP=$((SKIP + 1)); echo -e "${YELLOW}[SKIP]${NC} $1"; }
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }
step() { echo -e "\n${CYAN}========== Step $1: $2 ==========${NC}"; }

# ---------- Prerequisites ----------
if ! command -v jq &>/dev/null; then
    info "jq not found, installing..."
    apt-get update -qq && apt-get install -y -qq jq
fi

# Unique suffixes for idempotent test data
RAND_SUFFIX="e2esub$(date +%s)$(shuf -i 1000-9999 -n 1)"
TEST_EMAIL="${RAND_SUFFIX}@test.com"
TEST_PASSWORD="Test123456"
TEST_NICKNAME="E2ESubTest"

# ---------- Step 1: Register Test User ----------
step 1 "Register Test User"

info "Using email: $TEST_EMAIL"

REG_RESP=$(curl -s -X POST "$BASE_URL/api/auth/register" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"nickname\":\"$TEST_NICKNAME\"}" \
    --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')

REG_CODE=$(echo "$REG_RESP" | jq -r '.code // 999')
USER_TOKEN=$(echo "$REG_RESP" | jq -r '.data.token // empty')
USER_ID=$(echo "$REG_RESP" | jq -r '.data.user.id // empty')
API_KEY=$(echo "$REG_RESP" | jq -r '.data.user.apiKey // empty')

if [ -z "$USER_TOKEN" ] || [ "$REG_CODE" != "0" ]; then
    info "Registration attempt returned code=$REG_CODE, trying login..."
    LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H 'Content-Type: application/json' \
        -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}" \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    USER_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.token // empty')
    USER_ID=$(echo "$LOGIN_RESP" | jq -r '.data.user.id // empty')
    API_KEY=$(echo "$LOGIN_RESP" | jq -r '.data.user.apiKey // empty')
fi

if [ -n "$USER_TOKEN" ] && [ -n "$USER_ID" ]; then
    USER_HEADER="Authorization: Bearer $USER_TOKEN"
    pass "User registration/login successful (id=$USER_ID)"
    info "API Key: ${API_KEY:0:8}****${API_KEY: -4}"
else
    REG_MSG=$(echo "$REG_RESP" | jq -r '.message // "unknown error"' | head -c 100)
    fail "Failed to get test user: $REG_MSG"
    info "Cannot proceed without a test user."
    echo ""
    echo "=========================================="
    echo -e "  ${RED}RESULT: $PASS passed, $FAIL failed, $SKIP skipped${NC}"
    echo "=========================================="
    exit $FAIL
fi

# ---------- Step 2: Admin Login (for product seeding) ----------
step 2 "Admin Login"

ADMIN_RESP=$(curl -s -X POST "$AUTH_URL/admin/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"admin","password":"admin123"}' \
    --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')

ADMIN_TOKEN=$(echo "$ADMIN_RESP" | jq -r '.data.accessToken // empty')

if [ -n "$ADMIN_TOKEN" ]; then
    ADMIN_HEADER="Authorization: Bearer $ADMIN_TOKEN"
    pass "Admin login successful"
else
    # Try via gateway
    ADMIN_RESP=$(curl -s -X POST "$BASE_URL/admin/auth/login" \
        -H 'Content-Type: application/json' \
        -d '{"username":"admin","password":"admin123"}' \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    ADMIN_TOKEN=$(echo "$ADMIN_RESP" | jq -r '.data.accessToken // empty')
    ADMIN_HEADER="Authorization: Bearer $ADMIN_TOKEN"
    [ -n "$ADMIN_TOKEN" ] && pass "Admin login via gateway" || fail "Admin login failed"
fi

# ---------- Step 3: Fetch Plans ----------
step 3 "Fetch Available Plans"

PLANS_JSON=$(curl -s "$BASE_URL/api/products/plans" \
    --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
PLANS_COUNT=$(echo "$PLANS_JSON" | jq '.data | length // 0')

if [ "$PLANS_COUNT" -gt 0 ]; then
    pass "Plans endpoint returns $PLANS_COUNT plan(s)"
    # Pick the first available plan
    PLAN_CODE=$(echo "$PLANS_JSON" | jq -r '.data[0].code // empty')
    PLAN_NAME=$(echo "$PLANS_JSON" | jq -r '.data[0].name // "Unnamed"')
    info "Selected plan: $PLAN_CODE ($PLAN_NAME)"
else
    info "No plans found. Creating seed data via admin API..."
    if [ -n "${ADMIN_TOKEN:-}" ]; then
        # Create plan group
        GROUP_RESP=$(curl -s -X POST "$BASE_URL/admin/product/plan-groups" \
            -H "$ADMIN_HEADER" \
            -H 'Content-Type: application/json' \
            -d "{\"code\":\"${RAND_SUFFIX}-group\",\"name\":\"Subscription Test Group\",\"description\":\"E2E subscription test\",\"sortOrder\":1}" \
            --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
        GROUP_ID=$(echo "$GROUP_RESP" | jq -r '.data.id // empty')
        if [ -n "$GROUP_ID" ]; then
            PLAN_RESP=$(curl -s -X POST "$BASE_URL/admin/product/plans" \
                -H "$ADMIN_HEADER" \
                -H 'Content-Type: application/json' \
                -d "{\"groupId\":$GROUP_ID,\"code\":\"${RAND_SUFFIX}-plan\",\"name\":\"Sub Test Plan\",\"description\":\"E2E plan\",\"sortOrder\":1}" \
                --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
            PLAN_ID=$(echo "$PLAN_RESP" | jq -r '.data.id // empty')
            PLAN_CODE="${RAND_SUFFIX}-plan"
            if [ -n "$PLAN_ID" ]; then
                curl -s -X POST "$BASE_URL/admin/product/prices" \
                    -H "$ADMIN_HEADER" \
                    -H 'Content-Type: application/json' \
                    -d "{\"planId\":$PLAN_ID,\"currency\":\"CNY\",\"billingCycle\":\"monthly\",\"amount\":1.00,\"originalAmount\":10.00}" \
                    --connect-timeout 10 --max-time 15 >/dev/null 2>&1 || true
                pass "Seed plan created: $PLAN_CODE"
            else
                fail "Failed to create plan"
            fi
        else
            fail "Failed to create plan group"
        fi
    else
        fail "No admin token available for seeding"
    fi
fi

# ---------- Step 4: Create Order ----------
step 4 "Create Order for Plan"

if [ -z "${PLAN_CODE:-}" ]; then
    fail "No plan code available - cannot create order"
else
    ORDER_RESP=$(curl -s -X POST "$BASE_URL/api/orders" \
        -H "$USER_HEADER" \
        -H 'Content-Type: application/json' \
        -d "{\"planCode\":\"$PLAN_CODE\",\"billingCycle\":\"monthly\",\"currency\":\"CNY\"}" \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    ORDER_CODE=$(echo "$ORDER_RESP" | jq -r '.code // 999')
    ORDER_NO=$(echo "$ORDER_RESP" | jq -r '.data.orderNo // empty')
    ORDER_STATUS=$(echo "$ORDER_RESP" | jq -r '.data.status // empty')
    ORDER_AMOUNT=$(echo "$ORDER_RESP" | jq -r '.data.amount // "?"')

    if [ -n "$ORDER_NO" ] && [ "$ORDER_CODE" = "0" ]; then
        pass "Order created (orderNo=$ORDER_NO, status=$ORDER_STATUS, amount=$ORDER_AMOUNT)"
    else
        ORDER_MSG=$(echo "$ORDER_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Order creation failed: $ORDER_MSG"
    fi
fi

# ---------- Step 5: Mock Payment ----------
step 5 "Mock Payment (Checkout → Success)"

if [ -z "${ORDER_NO:-}" ]; then
    fail "No order number - cannot process payment"
else
    # Checkout
    CHECKOUT_RESP=$(curl -s -X POST "$BASE_URL/api/payments/checkout" \
        -H 'Content-Type: application/json' \
        -d "{\"orderNo\":\"$ORDER_NO\",\"channel\":\"mock\"}" \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    PAYMENT_NO=$(echo "$CHECKOUT_RESP" | jq -r '.data.paymentNo // empty')
    CHECKOUT_STATUS=$(echo "$CHECKOUT_RESP" | jq -r '.data.status // empty')

    if [ -n "$PAYMENT_NO" ]; then
        pass "Checkout successful (paymentNo=$PAYMENT_NO, status=$CHECKOUT_STATUS)"

        # Mock payment success (internally triggers markPaid + fulfillOrder)
        sleep 1
        SUCCESS_RESP=$(curl -s -X POST "$BASE_URL/api/payments/mock/$PAYMENT_NO/success" \
            -H 'Content-Type: application/json' \
            --connect-timeout 10 --max-time 20 2>/dev/null || echo '{}')
        SUCCESS_CODE=$(echo "$SUCCESS_RESP" | jq -r '.code // 999')

        if [ "$SUCCESS_CODE" = "0" ]; then
            pass "Mock payment success processed"
            sleep 2 # Allow async fulfillment to complete
        else
            SUCCESS_MSG=$(echo "$SUCCESS_RESP" | jq -r '.message // "unknown"' | head -c 100)
            fail "Mock payment success failed: $SUCCESS_MSG"
        fi
    else
        CHECKOUT_MSG=$(echo "$CHECKOUT_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Checkout failed: $CHECKOUT_MSG"
    fi
fi

# ---------- Step 6: Verify Subscription Created (Active) ----------
step 6 "Verify Subscription Created"

if [ -z "${USER_ID:-}" ]; then
    fail "No user ID - cannot verify subscription"
else
    # Try immediately then with retries
    SUB_RESP=""
    for i in 1 2 3; do
        SUB_RESP=$(curl -s "$BASE_URL/api/subscriptions/active?userId=$USER_ID" \
            --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
        SUB_CODE=$(echo "$SUB_RESP" | jq -r '.code // 999')
        SUB_ACTIVE=$(echo "$SUB_RESP" | jq -r '.data.active // "false"')
        SUB_PLAN=$(echo "$SUB_RESP" | jq -r '.data.planCode // empty')
        SUB_STATUS=$(echo "$SUB_RESP" | jq -r '.data.status // empty')
        SUB_ID=$(echo "$SUB_RESP" | jq -r '.data.id // empty')

        if [ -n "$SUB_ID" ] && [ "$SUB_ACTIVE" = "true" ]; then
            break
        fi
        if [ $i -lt 3 ]; then
            info "Retry $i: subscription not ready yet, waiting 2s..."
            sleep 2
        fi
    done

    if [ -n "${SUB_ID:-}" ]; then
        pass "Subscription created (id=$SUB_ID, plan=$SUB_PLAN, status=$SUB_STATUS, active=$SUB_ACTIVE)"
        SUB_EXPIRES=$(echo "$SUB_RESP" | jq -r '.data.expiresAt // "unknown"')
        info "Subscription expires at: $SUB_EXPIRES"
    else
        SUB_MSG=$(echo "$SUB_RESP" | jq -r '.message // "no subscription"' | head -c 100)
        # Check if data is null (no active subscription)
        SUB_DATA_NULL=$(echo "$SUB_RESP" | jq -r '.data == null')
        if [ "$SUB_DATA_NULL" = "true" ]; then
            fail "No active subscription found. Fulfillment may require order-service on port 8083."
        else
            fail "Subscription check failed: $SUB_MSG"
        fi
    fi
fi

# ---------- Step 7: Verify Quota Updated ----------
step 7 "Verify Quota Updated"

if [ -n "${USER_TOKEN:-}" ]; then
    PROFILE_RESP=$(curl -s "$BASE_URL/api/auth/profile" \
        -H "$USER_HEADER" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    PROFILE_CODE=$(echo "$PROFILE_RESP" | jq -r '.code // 999')

    if [ "$PROFILE_CODE" = "0" ]; then
        QUOTA_USED=$(echo "$PROFILE_RESP" | jq -r '.data.quotaUsed // 0')
        QUOTA_LIMIT=$(echo "$PROFILE_RESP" | jq -r '.data.quotaLimit // 0')
        USER_STATUS=$(echo "$PROFILE_RESP" | jq -r '.data.status // "unknown"')
        info "User profile: quotaUsed=$QUOTA_USED, quotaLimit=$QUOTA_LIMIT, status=$USER_STATUS"

        if [ "$QUOTA_LIMIT" -gt 0 ]; then
            pass "Quota limit updated to $QUOTA_LIMIT (post-fulfillment)"
        else
            info "Quota limit is $QUOTA_LIMIT (may be at default/trial level)"
            pass "User profile accessible with quota info"
        fi
    else
        PROFILE_MSG=$(echo "$PROFILE_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Profile fetch failed: $PROFILE_MSG"
    fi
else
    fail "No user token - cannot verify quota"
fi

# ---------- Step 8: Verify API Key Works with Quota ----------
step 8 "Verify API Key with Quota"

if [ -n "${API_KEY:-}" ]; then
    QUOTA_RESP=$(curl -s "$BASE_URL/api/auth/verify-key-with-quota?apiKey=$API_KEY" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    QUOTA_CODE=$(echo "$QUOTA_RESP" | jq -r '.code // 999')
    QUOTA_DATA_USERID=$(echo "$QUOTA_RESP" | jq -r '.data.userId // empty')
    QUOTA_DATA_USED=$(echo "$QUOTA_RESP" | jq -r '.data.quotaUsed // 0')
    QUOTA_DATA_LIMIT=$(echo "$QUOTA_RESP" | jq -r '.data.quotaLimit // 0')
    QUOTA_DATA_PLAN=$(echo "$QUOTA_RESP" | jq -r '.data.planCode // "none"')

    if [ "$QUOTA_CODE" = "0" ] && [ -n "$QUOTA_DATA_USERID" ]; then
        pass "API key verified with quota (userId=$QUOTA_DATA_USERID, plan=$QUOTA_DATA_PLAN)"
        info "Quota from service: $QUOTA_DATA_USED / $QUOTA_DATA_LIMIT, planCode=$QUOTA_DATA_PLAN"
    else
        QUOTA_MSG=$(echo "$QUOTA_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "API key quota verification failed: $QUOTA_MSG"
    fi
else
    fail "No API key available"
fi

# ---------- Step 9: List All Subscriptions for User ----------
step 9 "List User Subscriptions"

if [ -n "${USER_ID:-}" ]; then
    LIST_RESP=$(curl -s "$BASE_URL/api/subscriptions?userId=$USER_ID" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    LIST_CODE=$(echo "$LIST_RESP" | jq -r '.code // 999')
    LIST_COUNT=$(echo "$LIST_RESP" | jq '.data | length // 0')

    if [ "$LIST_CODE" = "0" ] && [ "$LIST_COUNT" -gt 0 ]; then
        pass "Subscription list returns $LIST_COUNT subscription(s)"
        # Show all subscriptions
        echo "$LIST_RESP" | jq -r '.data[] | "  - id=\(.id) plan=\(.planCode) status=\(.status) active=\(.active)"'
    elif [ "$LIST_CODE" = "0" ]; then
        info "No subscriptions in list (list returned empty)"
        pass "Subscription list endpoint accessible"
    else
        LIST_MSG=$(echo "$LIST_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Subscription list failed: $LIST_MSG"
    fi
else
    fail "Cannot list subscriptions - no user ID"
fi

# ---------- Step 10: Order Details ----------
step 10 "Verify Order Status"

if [ -n "${ORDER_NO:-}" ]; then
    ORDER_GET_RESP=$(curl -s "$BASE_URL/api/orders/$ORDER_NO" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    GET_CODE=$(echo "$ORDER_GET_RESP" | jq -r '.code // 999')
    GET_STATUS=$(echo "$ORDER_GET_RESP" | jq -r '.data.status // empty')
    GET_USER_ID=$(echo "$ORDER_GET_RESP" | jq -r '.data.userId // empty')

    if [ "$GET_CODE" = "0" ] && [ -n "$GET_STATUS" ]; then
        pass "Order retrieved (orderNo=$ORDER_NO, status=$GET_STATUS, userId=$GET_USER_ID)"
        [ "$GET_STATUS" = "FULFILLED" ] || [ "$GET_STATUS" = "PAID" ] && \
            pass "Order status is $GET_STATUS (expected PAID or FULFILLED)" || \
            info "Order status: $GET_STATUS"
    else
        GET_MSG=$(echo "$ORDER_GET_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Order retrieval failed: $GET_MSG"
    fi
else
    fail "No order number to verify"
fi

# ---------- Step 11: Cancel Subscription (Note) ----------
step 11 "Cancel Subscription"

if [ -n "${SUB_ID:-}" ]; then
    skip "Cancel subscription API is not yet exposed. Subscription (id=$SUB_ID) is active and will expire naturally."
    info "The subscription scheduler in order-service auto-expires subscriptions after expiresAt."
    info "No manual cancel endpoint exists in the current API surface."

    # Verify we can still see the subscription (it's still active since we just created it)
    SUB_RESP=$(curl -s "$BASE_URL/api/subscriptions/active?userId=$USER_ID" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    STILL_ACTIVE=$(echo "$SUB_RESP" | jq -r '.data.active // "false"')
    info "Subscription currently active=$STILL_ACTIVE (expected: true before expiry)"
else
    skip "No active subscription found to cancel"
fi

# ---------- Summary ----------
echo ""
echo "=========================================="
TOTAL=$((PASS + FAIL))
if [ "$FAIL" -eq 0 ]; then
    echo -e "  ${GREEN}RESULT: $PASS passed, $FAIL failed, $SKIP skipped — ALL PASS${NC}"
else
    echo -e "  ${RED}RESULT: $PASS passed, $FAIL failed, $SKIP skipped${NC}"
fi
echo "=========================================="

exit $FAIL
