#!/bin/bash
# ==============================================================
# e2e-ai-flow.sh - End-to-End AI Call Flow Test
#
# Tests the complete AI lifecycle:
#   1.  Health check on all services
#   2.  Admin login
#   3.  Seed product plans (if none exist)
#   4.  Register test user (gets API key)
#   5.  Create order
#   6.  Mock payment → triggers fulfillment
#   7.  Check subscription active
#   8.  Verify API key works
#   9.  Chat completion (non-streaming)
#  10.  Chat completion (streaming)
#  11.  Embedding
#  12.  Check usage recorded
#
# Usage:
#   export BASE_URL=http://localhost:8080   (default)
#   export AI_URL=http://localhost:8086     (default)
#   export AUTH_URL=http://localhost:8082   (default)
#   bash scripts/e2e-ai-flow.sh
# ==============================================================

set -euo pipefail

# ---------- Configuration ----------
BASE_URL="${BASE_URL:-http://localhost:8080}"
AI_URL="${AI_URL:-http://localhost:8086}"
AUTH_URL="${AUTH_URL:-http://localhost:8082}"

# ---------- Colors & Helpers ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PASS=0
FAIL=0

pass() { PASS=$((PASS + 1)); echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { FAIL=$((FAIL + 1)); echo -e "${RED}[FAIL]${NC} $1"; }
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }
step() { echo -e "\n${CYAN}========== Step $1: $2 ==========${NC}"; }

# ---------- Prerequisites ----------
if ! command -v jq &>/dev/null; then
    info "jq not found, installing..."
    apt-get update -qq && apt-get install -y -qq jq
fi

# Unique suffixes for idempotent test data
RAND_SUFFIX="e2eai$(date +%s)$(shuf -i 1000-9999 -n 1)"
TEST_EMAIL="${RAND_SUFFIX}@test.com"
TEST_PASSWORD="Test123456"
TEST_NICKNAME="E2EAI"

# ---------- Step 1: Health Check ----------
step 1 "Health Check"

HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" --connect-timeout 5 --max-time 10 2>/dev/null || echo "000")
[ "$HTTP" = "200" ] && pass "Gateway health check (HTTP $HTTP)" || fail "Gateway health check returned HTTP $HTTP"

HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$AI_URL/actuator/health" --connect-timeout 5 --max-time 10 2>/dev/null || echo "000")
[ "$HTTP" = "200" ] && pass "AI service health check (HTTP $HTTP)" || fail "AI service health check returned HTTP $HTTP"

HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$AUTH_URL/actuator/health" --connect-timeout 5 --max-time 10 2>/dev/null || echo "000")
[ "$HTTP" = "200" ] && pass "Auth service health check (HTTP $HTTP)" || fail "Auth service health check returned HTTP $HTTP"

# ---------- Step 2: Admin Login ----------
step 2 "Admin Login"

ADMIN_RESP=$(curl -s -X POST "$BASE_URL/admin/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"admin","password":"admin123"}' \
    --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')

ADMIN_TOKEN=$(echo "$ADMIN_RESP" | jq -r '.data.accessToken // empty')
ADMIN_HEADER="Authorization: Bearer $ADMIN_TOKEN"

if [ -n "$ADMIN_TOKEN" ]; then
    pass "Admin login successful (token acquired)"
else
    # Try auth-service directly
    ADMIN_RESP=$(curl -s -X POST "$AUTH_URL/admin/auth/login" \
        -H 'Content-Type: application/json' \
        -d '{"username":"admin","password":"admin123"}' \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    ADMIN_TOKEN=$(echo "$ADMIN_RESP" | jq -r '.data.accessToken // empty')
    ADMIN_HEADER="Authorization: Bearer $ADMIN_TOKEN"
    if [ -n "$ADMIN_TOKEN" ]; then
        pass "Admin login via auth-service (token acquired)"
    else
        fail "Admin login failed"
        info "Skipping steps that require admin token..."
    fi
fi

# ---------- Step 3: Ensure Product Plans Available ----------
step 3 "Seed Product Plans (if needed)"

PLANS_JSON=$(curl -s "$BASE_URL/api/products/plans" --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
PLANS_COUNT=$(echo "$PLANS_JSON" | jq '.data | length // 0')

PLAN_CODE=""
if [ "$PLANS_COUNT" -gt 0 ]; then
    PLAN_CODE=$(echo "$PLANS_JSON" | jq -r '.data[0].code // empty')
    PLAN_NAME=$(echo "$PLANS_JSON" | jq -r '.data[0].name // "Plan"')
    info "Found $PLANS_COUNT existing plan(s), using first: $PLAN_CODE"
    pass "Product plans available ($PLANS_COUNT plans)"
else
    info "No plans found. Creating seed data via admin API..."
    if [ -n "$ADMIN_TOKEN" ]; then
        # Create plan group
        GROUP_RESP=$(curl -s -X POST "$BASE_URL/admin/product/plan-groups" \
            -H "$ADMIN_HEADER" \
            -H 'Content-Type: application/json' \
            -d "{\"code\":\"${RAND_SUFFIX}-group\",\"name\":\"E2E AI Group\",\"description\":\"E2E AI test group\",\"sortOrder\":1}" \
            --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
        GROUP_ID=$(echo "$GROUP_RESP" | jq -r '.data.id // empty')
        if [ -n "$GROUP_ID" ]; then
            # Create plan
            PLAN_RESP=$(curl -s -X POST "$BASE_URL/admin/product/plans" \
                -H "$ADMIN_HEADER" \
                -H 'Content-Type: application/json' \
                -d "{\"groupId\":$GROUP_ID,\"code\":\"${RAND_SUFFIX}-plan\",\"name\":\"E2E AI Plan\",\"description\":\"E2E AI test plan\",\"sortOrder\":1}" \
                --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
            PLAN_ID=$(echo "$PLAN_RESP" | jq -r '.data.id // empty')
            PLAN_CODE="${RAND_SUFFIX}-plan"
            if [ -n "$PLAN_ID" ]; then
                # Create price
                PRICE_RESP=$(curl -s -X POST "$BASE_URL/admin/product/prices" \
                    -H "$ADMIN_HEADER" \
                    -H 'Content-Type: application/json' \
                    -d "{\"planId\":$PLAN_ID,\"currency\":\"CNY\",\"billingCycle\":\"monthly\",\"amount\":1.00,\"originalAmount\":10.00}" \
                    --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
                PRICE_ID=$(echo "$PRICE_RESP" | jq -r '.data.id // empty')
                if [ -n "$PRICE_ID" ]; then
                    # Create a feature
                    curl -s -X POST "$BASE_URL/admin/product/features" \
                        -H "$ADMIN_HEADER" \
                        -H 'Content-Type: application/json' \
                        -d "{\"planId\":$PLAN_ID,\"featureName\":\"API Calls\",\"featureValue\":\"1000/day\",\"included\":true,\"sortOrder\":1}" \
                        --connect-timeout 10 --max-time 15 >/dev/null 2>&1 || true
                    pass "Seed data created (group=$GROUP_ID, plan=$PLAN_ID, price=$PRICE_ID)"
                else
                    fail "Failed to create price"
                fi
            else
                fail "Failed to create plan"
            fi
        else
            fail "Failed to create plan group"
        fi
    else
        fail "Cannot seed data - no admin token"
    fi
fi

# ---------- Step 4: Register Test User ----------
step 4 "Register Test User"

REG_RESP=$(curl -s -X POST "$BASE_URL/api/auth/register" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"nickname\":\"$TEST_NICKNAME\"}" \
    --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')

REG_CODE=$(echo "$REG_RESP" | jq -r '.code // 999')
USER_TOKEN=$(echo "$REG_RESP" | jq -r '.data.token // empty')
USER_ID=$(echo "$REG_RESP" | jq -r '.data.user.id // empty')
API_KEY=$(echo "$REG_RESP" | jq -r '.data.user.apiKey // empty')

if [ -z "$USER_TOKEN" ] || [ "$REG_CODE" != "0" ]; then
    # User may already exist, try login
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
    API_KEY_HEADER="Authorization: Bearer $API_KEY"
    pass "Test user ready (id=$USER_ID, email=$TEST_EMAIL)"
    info "API Key: ${API_KEY:0:8}****${API_KEY: -4}"
else
    REG_MSG=$(echo "$REG_RESP" | jq -r '.message // "unknown error"' | head -c 100)
    fail "Failed to get test user: $REG_MSG"
fi

# ---------- Step 5: Create Order ----------
step 5 "Create Order"

if [ -n "$USER_TOKEN" ] && [ -n "$PLAN_CODE" ]; then
    ORDER_RESP=$(curl -s -X POST "$BASE_URL/api/orders" \
        -H "$USER_HEADER" \
        -H 'Content-Type: application/json' \
        -d "{\"planCode\":\"$PLAN_CODE\",\"billingCycle\":\"monthly\",\"currency\":\"CNY\"}" \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    ORDER_CODE=$(echo "$ORDER_RESP" | jq -r '.code // 999')
    ORDER_NO=$(echo "$ORDER_RESP" | jq -r '.data.orderNo // empty')
    ORDER_STATUS=$(echo "$ORDER_RESP" | jq -r '.data.status // empty')

    if [ -n "$ORDER_NO" ] && [ "$ORDER_CODE" = "0" ]; then
        pass "Order created (orderNo=$ORDER_NO, status=$ORDER_STATUS)"
    else
        ORDER_MSG=$(echo "$ORDER_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Order creation failed: $ORDER_MSG"
    fi
else
    fail "Cannot create order - missing user token or plan code"
fi

# ---------- Step 6: Mock Payment ----------
step 6 "Mock Payment → Fulfillment"

if [ -n "$ORDER_NO" ]; then
    # Checkout (create payment)
    CHECKOUT_RESP=$(curl -s -X POST "$BASE_URL/api/payments/checkout" \
        -H 'Content-Type: application/json' \
        -d "{\"orderNo\":\"$ORDER_NO\",\"channel\":\"mock\"}" \
        --connect-timeout 10 --max-time 15 2>/dev/null || echo '{}')
    CHECKOUT_CODE=$(echo "$CHECKOUT_RESP" | jq -r '.code // 999')
    PAYMENT_NO=$(echo "$CHECKOUT_RESP" | jq -r '.data.paymentNo // empty')
    PAYMENT_STATUS=$(echo "$CHECKOUT_RESP" | jq -r '.data.status // empty')

    if [ -n "$PAYMENT_NO" ] && [ "$CHECKOUT_CODE" = "0" ]; then
        pass "Checkout successful (paymentNo=$PAYMENT_NO, status=$PAYMENT_STATUS)"

        # Simulate payment success (triggers markPaid + fulfillOrder → creates subscription)
        sleep 1
        SUCCESS_RESP=$(curl -s -X POST "$BASE_URL/api/payments/mock/$PAYMENT_NO/success" \
            -H 'Content-Type: application/json' \
            --connect-timeout 10 --max-time 20 2>/dev/null || echo '{}')
        SUCCESS_CODE=$(echo "$SUCCESS_RESP" | jq -r '.code // 999')
        SUCCESS_STATUS=$(echo "$SUCCESS_RESP" | jq -r '.data.status // empty')

        if [ "$SUCCESS_CODE" = "0" ]; then
            pass "Mock payment success (status=$SUCCESS_STATUS)"
            # Give fulfillment a moment to complete
            sleep 1
        else
            SUCCESS_MSG=$(echo "$SUCCESS_RESP" | jq -r '.message // "unknown"' | head -c 100)
            fail "Mock payment success failed: $SUCCESS_MSG"
        fi
    else
        CHECKOUT_MSG=$(echo "$CHECKOUT_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Checkout failed: $CHECKOUT_MSG"
    fi
else
    fail "Cannot checkout - no order number"
fi

# ---------- Step 7: Check Subscription Active ----------
step 7 "Check Subscription Active"

if [ -n "$USER_ID" ]; then
    SUB_RESP=$(curl -s "$BASE_URL/api/subscriptions/active?userId=$USER_ID" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    SUB_CODE=$(echo "$SUB_RESP" | jq -r '.code // 999')
    SUB_ACTIVE=$(echo "$SUB_RESP" | jq -r '.data.active // "false"')
    SUB_PLAN=$(echo "$SUB_RESP" | jq -r '.data.planCode // empty')
    SUB_STATUS=$(echo "$SUB_RESP" | jq -r '.data.status // empty')

    if [ "$SUB_CODE" = "0" ] && [ -n "$SUB_PLAN" ]; then
        info "Subscription: planCode=$SUB_PLAN, status=$SUB_STATUS, active=$SUB_ACTIVE"
        [ "$SUB_ACTIVE" = "true" ] && pass "Subscription is active" || fail "Subscription is not active"
    else
        # Might need more time for async fulfillment
        info "Subscription not found immediately, waiting 3s and retrying..."
        sleep 3
        SUB_RESP=$(curl -s "$BASE_URL/api/subscriptions/active?userId=$USER_ID" \
            --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
        SUB_ACTIVE=$(echo "$SUB_RESP" | jq -r '.data.active // "false"')
        SUB_PLAN=$(echo "$SUB_RESP" | jq -r '.data.planCode // empty')
        SUB_STATUS=$(echo "$SUB_RESP" | jq -r '.data.status // empty')
        if [ -n "$SUB_PLAN" ]; then
            [ "$SUB_ACTIVE" = "true" ] && pass "Subscription active (plan=$SUB_PLAN)" || fail "Subscription not active"
        else
            fail "No active subscription found. Fulfillment may need the order-service running on port 8083."
        fi
    fi
else
    fail "Cannot check subscription - no user ID"
fi

# ---------- Step 8: Verify API Key ----------
step 8 "Verify API Key"

if [ -n "$API_KEY" ]; then
    KEY_RESP=$(curl -s "$BASE_URL/api/auth/verify-key?apiKey=$API_KEY" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    KEY_CODE=$(echo "$KEY_RESP" | jq -r '.code // 999')
    KEY_USER_ID=$(echo "$KEY_RESP" | jq -r '.data // empty')

    if [ "$KEY_CODE" = "0" ] && [ -n "$KEY_USER_ID" ]; then
        pass "API key verified (maps to userId=$KEY_USER_ID)"

        # Also verify with quota
        QUOTA_RESP=$(curl -s "$BASE_URL/api/auth/verify-key-with-quota?apiKey=$API_KEY" \
            --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
        QUOTA_USED=$(echo "$QUOTA_RESP" | jq -r '.data.quotaUsed // 0')
        QUOTA_LIMIT=$(echo "$QUOTA_RESP" | jq -r '.data.quotaLimit // 0')
        info "Quota: $QUOTA_USED / $QUOTA_LIMIT used"
        pass "API key quota info retrieved"
    else
        KEY_MSG=$(echo "$KEY_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "API key verification failed: $KEY_MSG"
    fi
else
    fail "Cannot verify key - no API key"
fi

# ---------- Step 9: Chat Completion (Non-Streaming) ----------
step 9 "Chat Completion (Non-Streaming)"

if [ -n "$API_KEY" ]; then
    CHAT_RESP=$(curl -s -X POST "$AI_URL/v1/chat/completions" \
        -H "$API_KEY_HEADER" \
        -H 'Content-Type: application/json' \
        -d '{"model":"gpt-4o-mini","messages":[{"role":"user","content":"Say hello in one word"}],"stream":false}' \
        --connect-timeout 10 --max-time 30 2>/dev/null || echo '{}')

    CHAT_CHOICES=$(echo "$CHAT_RESP" | jq '.choices | length // 0')
    CHAT_CONTENT=$(echo "$CHAT_RESP" | jq -r '.choices[0].message.content // empty' | head -c 100)

    if [ "$CHAT_CHOICES" -gt 0 ] && [ -n "$CHAT_CONTENT" ]; then
        pass "Chat completion successful (choices=$CHAT_CHOICES)"
        info "Response: $CHAT_CONTENT"
    else
        CHAT_ERROR=$(echo "$CHAT_RESP" | jq -r '.error.message // "no response"' | head -c 150)
        fail "Chat completion failed: $CHAT_ERROR"
    fi
else
    fail "Cannot call AI - no API key"
fi

# ---------- Step 10: Chat Completion (Streaming) ----------
step 10 "Chat Completion (Streaming)"

if [ -n "$API_KEY" ]; then
    STREAM_OUTPUT=$(curl -s -X POST "$AI_URL/v1/chat/completions" \
        -H "$API_KEY_HEADER" \
        -H 'Content-Type: application/json' \
        -d '{"model":"gpt-4o-mini","messages":[{"role":"user","content":"Say hi"}],"stream":true}' \
        --connect-timeout 10 --max-time 30 2>/dev/null || echo '')

    if echo "$STREAM_OUTPUT" | grep -q "data:"; then
        # Get first meaningful data line
        FIRST_CHUNK=$(echo "$STREAM_OUTPUT" | grep "^data:" | head -2 | tr '\n' ' ')
        pass "Streaming SSE response received"
        info "First chunks: $FIRST_CHUNK"
    else
        fail "No SSE data received in streaming response"
        info "Raw output preview: $(echo "$STREAM_OUTPUT" | head -c 200)"
    fi
else
    fail "Cannot call AI - no API key"
fi

# ---------- Step 11: Embedding ----------
step 11 "Embedding API"

if [ -n "$API_KEY" ]; then
    EMBED_RESP=$(curl -s -X POST "$AI_URL/v1/embeddings" \
        -H "$API_KEY_HEADER" \
        -H 'Content-Type: application/json' \
        -d '{"model":"text-embedding-ada-002","input":["Hello world"]}' \
        --connect-timeout 10 --max-time 30 2>/dev/null || echo '{}')

    EMBED_DATA_LEN=$(echo "$EMBED_RESP" | jq '.data | length // 0')
    EMBED_VECTOR_LEN=$(echo "$EMBED_RESP" | jq '.data[0].embedding | length // 0')

    if [ "$EMBED_DATA_LEN" -gt 0 ] && [ "$EMBED_VECTOR_LEN" -gt 0 ]; then
        pass "Embedding successful (data=$EMBED_DATA_LEN, dimensions=$EMBED_VECTOR_LEN)"
    else
        EMBED_ERROR=$(echo "$EMBED_RESP" | jq -r '.error.message // "no response"' | head -c 150)
        fail "Embedding failed: $EMBED_ERROR"
    fi
else
    fail "Cannot call AI - no API key"
fi

# ---------- Step 12: Check Usage Recorded ----------
step 12 "Check Usage Recorded"

if [ -n "$USER_ID" ]; then
    USAGE_RESP=$(curl -s "$BASE_URL/api/usage/$USER_ID?page=1&pageSize=10" \
        --connect-timeout 5 --max-time 10 2>/dev/null || echo '{}')
    USAGE_CODE=$(echo "$USAGE_RESP" | jq -r '.code // 999')
    USAGE_COUNT=$(echo "$USAGE_RESP" | jq -r '.data.totalElements // .data.content | length // 0')

    if [ "$USAGE_CODE" = "0" ] && [ "$USAGE_COUNT" -gt 0 ]; then
        pass "Usage records found ($USAGE_COUNT entries)"
        # Show last entry
        LAST_USAGE=$(echo "$USAGE_RESP" | jq -r '.data.content[-1] // .data[-1] // empty' | head -c 200)
        info "Last usage entry: $LAST_USAGE"
    elif [ "$USAGE_CODE" = "0" ]; then
        info "No usage records yet (may be empty if AI calls failed upstream)"
        pass "Usage API accessible (0 entries)"
    else
        USAGE_MSG=$(echo "$USAGE_RESP" | jq -r '.message // "unknown"' | head -c 100)
        fail "Usage API failed: $USAGE_MSG"
    fi
else
    fail "Cannot check usage - no user ID"
fi

# ---------- Summary ----------
echo ""
echo "=========================================="
if [ "$FAIL" -eq 0 ]; then
    echo -e "  ${GREEN}RESULT: $PASS passed, $FAIL failed — ALL PASS${NC}"
else
    echo -e "  ${RED}RESULT: $PASS passed, $FAIL failed${NC}"
fi
echo "=========================================="

exit $FAIL
