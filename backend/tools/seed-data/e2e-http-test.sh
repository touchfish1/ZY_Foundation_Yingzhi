#!/bin/bash
# HTTP 端到端测试 - 模拟10个用户走完整流程
set -e

API="http://localhost:8080"
USER_COUNT=10
PASS="test123456"

echo "=========================================="
echo "E2E HTTP Test - $USER_COUNT users"
echo "=========================================="

# Array to store tokens and user IDs
declare -a TOKENS
declare -a USER_IDS

# Step 1: Register 10 users
echo ""
echo "=== Step 1: Register $USER_COUNT users ==="
for i in $(seq 1 $USER_COUNT); do
  EMAIL="e2e-user${i}@test.com"
  RESP=$(curl -s -X POST "$API/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\",\"nickname\":\"E2E用户${i}\"}")
  TOKEN=$(echo "$RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  USR_ID=$(echo "$RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
  if [ -n "$TOKEN" ]; then
    TOKENS[$i]=$TOKEN
    USER_IDS[$i]=$USR_ID
    echo "  [$i] $EMAIL registered, USR_ID=$USR_ID, token OK"
  else
    echo "  [$i] $EMAIL FAILED: $(echo $RESP | head -c 100)"
  fi
done

# Step 2: Login each user (verify credentials work)
echo ""
echo "=== Step 2: Login verification ==="
for i in $(seq 1 $USER_COUNT); do
  EMAIL="e2e-user${i}@test.com"
  RESP=$(curl -s -X POST "$API/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\"}")
  TOKEN=$(echo "$RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$TOKEN" ]; then
    TOKENS[$i]=$TOKEN
    echo "  [$i] Login OK"
  else
    echo "  [$i] Login FAILED"
  fi
done

# Step 3: Query products
echo ""
echo "=== Step 3: Query products ==="
RESP=$(curl -s "$API/api/products/plans")
echo "  Products: $(echo $RESP | grep -o '"code":"[^"]*"' | wc -l) plans found"
PLAN_CODE=$(echo $RESP | grep -o '"code":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "  First plan: $PLAN_CODE"
[ -z "$PLAN_CODE" ] && PLAN_CODE="premium-monthly"

# Step 4-7: Each user creates order, pays, checks subscription
echo ""
echo "=== Step 4-7: Order → Payment → Fulfillment → Subscription ==="
for i in $(seq 1 $USER_COUNT); do
  TOKEN=${TOKENS[$i]}
  echo "  --- User $i ---"

  # Step 4: Create order
  RESP=$(curl -s -X POST "$API/api/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"planCode\":\"$PLAN_CODE\",\"billingCycle\":\"monthly\",\"currency\":\"CNY\"}")
  ORDER_NO=$(echo "$RESP" | grep -o '"orderNo":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$ORDER_NO" ]; then
    echo "    Order: $ORDER_NO"
  else
    echo "    Order FAILED: $(echo $RESP | head -c 80)"
    continue
  fi

  # Step 5: Create payment
  RESP=$(curl -s -X POST "$API/api/payments/checkout" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"orderNo\":\"$ORDER_NO\",\"channel\":\"mock\"}")
  PAY_NO=$(echo "$RESP" | grep -o '"paymentNo":"[^"]*"' | cut -d'"' -f4)
  MOCK_URL=$(echo "$RESP" | grep -o '"mockPayUrl":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$PAY_NO" ]; then
    echo "    Payment: $PAY_NO"
  else
    echo "    Payment FAILED: $(echo $RESP | head -c 80)"
    continue
  fi

  # Step 6: Complete mock payment
  sleep 0.2
  RESP=$(curl -s -X POST "$API$MOCK_URL")
  STATUS=$(echo "$RESP" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
  echo "    Mock pay result: $STATUS"

  # Step 7: Check subscription (use USR_ID from registration)
  USR_ID=${USER_IDS[$i]}
  if [ -n "$USR_ID" ]; then
    RESP=$(curl -s -H "Authorization: Bearer $TOKEN" "$API/api/subscriptions/active?userId=$USR_ID")
    SUB_STATUS=$(echo "$RESP" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "    Subscription: $SUB_STATUS"
  fi
done

# Step 8: Recharge balance
echo ""
echo "=== Step 8: Balance recharge ==="
for i in $(seq 1 $USER_COUNT); do
  USR_ID=${USER_IDS[$i]}
  if [ -n "$USR_ID" ]; then
    RESP=$(curl -s -X POST "$API/api/balance/$USR_ID/recharge?amount=100&description=测试充值")
    BAL=$(echo "$RESP" | grep -o '"balance":[0-9.]*' | cut -d: -f2)
    echo "  User $i: balance=$BAL"
  fi
done

# Step 9: Record usage
echo ""
echo "=== Step 9: Record usage ==="
for i in $(seq 1 $USER_COUNT); do
  USR_ID=${USER_IDS[$i]}
  if [ -n "$USR_ID" ]; then
    RESP=$(curl -s -X POST "$API/api/usage/record" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${TOKENS[$i]}" \
      -d "{\"userId\":$USR_ID,\"apiKey\":\"sk-test-$USR_ID\",\"apiPath\":\"/v1/chat/completions\",\"tokensIn\":500,\"tokensOut\":100,\"cost\":0.01,\"durationMs\":200,\"status\":\"success\"}")
    echo "  User $i: usage recorded ($(echo $RESP | grep -o '"code":[0-9]*'))"
  fi
done

# Step 10: Check audit logs
echo ""
echo "=== Step 10: Check audit logs ==="
for i in $(seq 1 3); do  # Just first 3 users
  USR_ID=${USER_IDS[$i]}
  if [ -n "$USR_ID" ]; then
    RESP=$(curl -s -H "Authorization: Bearer ${TOKENS[$i]}" "$API/api/logs/user/$USR_ID?page=1&pageSize=5")
    TOTAL=$(echo "$RESP" | grep -o '"totalElements":[0-9]*' | cut -d: -f2)
    echo "  User $i: audit logs = $TOTAL"
  fi
done

echo ""
echo "=========================================="
echo "E2E Test Complete!"
echo "=========================================="
