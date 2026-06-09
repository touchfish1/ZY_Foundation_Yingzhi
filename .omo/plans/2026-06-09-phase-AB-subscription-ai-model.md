# Phase A+B Implementation Plan: Subscription Fulfillment + AI Model Integration

**Date:** 2026-06-09
**Status:** Draft
**Version:** 1.0

> **For agentic workers:** Each task is atomic. Work in parallel where possible.
> All prompts to subagents MUST include all 6 sections: TASK, EXPECTED OUTCOME, REQUIRED TOOLS, MUST DO, MUST NOT DO, CONTEXT.

---

## Overview

Complete the platform's core revenue loop (subscription fulfillment) and core product capability (AI model calling) across 3 sequential sprints.

### Architecture Diagram (Target State)

```
User Request → /v1/chat/completions
                    │
                    ▼
          ┌─────────────────────┐
          │  API Key Auth Filter │  ← validates sk-*, resolves user
          └─────────┬───────────┘
                    ▼
          ┌─────────────────────┐
          │  Rate Limit Check   │  ← concurrency + rpmLimit enforcement
          └─────────┬───────────┘
                    ▼
          ┌─────────────────────┐
          │  Access Control     │  ← plan → model mapping check
          └─────────┬───────────┘
                    ▼
          ┌─────────────────────┐
          │  Model Router       │  ← route to correct provider
          └─────────┬───────────┘
                    ▼
          ┌─────────────────────┐
          │  ModelProvider      │  ← OpenAI / Anthropic / ...
          │  (stream + non-stream)│
          └─────────┬───────────┘
                    ▼
          ┌─────────────────────┐
          │  UsageService       │  ← record tokens, cost, duration
          └─────────────────────┘
```

### Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Model provider layer | Interface in `api` module, impls as sub-packages | Reuse shared DB access to product/plan tables; no new service |
| Model routing | Path-based + model-name-based routing config in DB | Dynamic, no redeploy required for new models |
| API Key auth | Custom Filter in `api` module spring security chain | Clean integration with existing Sa-Token setup |
| Rate limiting | Token Bucket per user (in-memory with Redis fallback) | Simple, effective, no extra infra |
| Subscription data | Extend `user_subscription` in `order-service` with FK fields | Follows existing microservice ownership pattern |

---

## Sprint 1: Fix Defects + Foundation Building

**Goal:** Stabilize existing flow, fix all blocking bugs, establish model provider interfaces.

**Estimated effort:** 3-4 days

### Task 1.1: Fix PaymentCallback Missing Fulfillment Trigger

**Files:**
- `backend/payment-service/src/main/java/com/zhangyuan/payment/adapter/in/rest/PaymentCallbackController.java`
- `backend/payment-service/src/main/java/com/zhangyuan/payment/application/service/PaymentApplicationService.java`

**Problem:** `PaymentCallbackController.handleCallback()` marks payment success but does NOT call `fulfillmentClient.fulfillOrder(orderNo)`. Only `mockSuccess()` does the full flow. Real channel callbacks will leave orders as PAID but unfulfilled.

**Fix:**
1. Extract the fulfillment flow from `mockSuccess()` into a shared `PaymentApplicationService.handlePaymentSuccess(paymentNo)` method:
   - Find Payment by paymentNo
   - Idempotency check
   - `payment.markSuccess()`
   - `fulfillmentClient.markPaid(orderNo)`
   - `compensationService.createFulfillEvent(paymentNo, orderNo)`
   - `fulfillmentClient.fulfillOrder(orderNo)` (with try/catch — compensation picks up failure)
2. `mockSuccess()` calls `handlePaymentSuccess()`
3. `handleCallback()` also calls `handlePaymentSuccess()`

**Acceptance:**
- `mockSuccess` flow unchanged behavior
- `handleCallback` triggers fulfillment after marking payment success
- Idempotent: calling twice does not double-fulfill
- Compensation event created for async retry

### Task 1.2: Add fulfilled_at Migration + Fix OrderMainEntity

**Files:**
- Create: `backend/order-service/src/main/resources/db/migration/V004__add_fulfilled_at_column.sql`
- Verify: `backend/order-service/src/main/java/com/zhangyuan/order/adapter/out/persistence/OrderMainEntity.java`

**Problem:** `OrderMainEntity.java` has a `fulfilledAt` field mapped to `fulfilled_at` column, but V001 migration does not include this column. This will cause JPA startup error.

**Fix:**
1. Create V004 migration:
   ```sql
   alter table order_main add column if not exists fulfilled_at timestamptz;
   ```
2. Verify `OrderMainEntity.fulfilledAt` is correctly annotated with `@Column(name = "fulfilled_at")`

**Acceptance:**
- `OrderMainEntity` JPA validation succeeds on startup
- Existing orders have `fulfilled_at = null` (nullable)
- New fulfilled orders populate the column

### Task 1.3: Create Typed OrderSnapshot Record

**Files:**
- Create: `backend/order-service/src/main/java/com/zhangyuan/order/dto/OrderSnapshot.java`
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/application/service/FulfillmentService.java`
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/application/service/OrderApplicationService.java`
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/domain/model/Order.java` (optional)

**Problem:** Snapshot JSON is stored/parsed as raw string. `FulfillmentService.extractFromSnapshot()` uses `indexOf`/`substring` — fragile and untyped.

**Fix:**
1. Create `OrderSnapshot` record:
   ```java
   package com.zhangyuan.order.dto;

   import java.math.BigDecimal;
   import java.time.Instant;

   public record OrderSnapshot(
       String planCode,
       String billingCycle,
       String currency,
       Instant createdAt,
       String planName,
       Integer validityDays,
       BigDecimal amount
   ) {}
   ```
2. In `FulfillmentService`, replace string parsing with Jackson `ObjectMapper.readValue(order.getSnapshotJson(), OrderSnapshot.class)`
3. In `OrderApplicationService.buildSnapshotFromFeignResponse()`, serialize the snapshot using `ObjectMapper.writeValueAsString()` instead of manual JSON building

**Acceptance:**
- Snapshot round-trips correctly: build → serialize → deserialize → same fields
- `FulfillmentService` extracts all fields from typed snapshot
- No string manipulation for snapshot parsing
- Existing snapshots in DB (stored as JSON) are backward-compatible

### Task 1.4: Add GET /api/orders List Endpoint

**Files:**
- Modify: `backend/api/src/main/java/com/zhangyuan/modules/order/adapter/in/rest/OrderPublicController.java`
- OR: `backend/order-service/src/main/java/com/zhangyuan/order/adapter/in/rest/OrderPublicController.java`

**Context:** Web frontend calls `GET /api/orders?userId=X` from both `dashboard.vue` and `dashboard/orders.vue`. The current controller only has `POST /api/orders` and `GET /api/orders/{orderNo}` — no list handler exists.

**Fix:**
In `OrderPublicController` add:
```java
@GetMapping
public ApiResponse<List<OrderResponse>> listOrders(@RequestParam Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);
    return ApiResponse.success(orders.stream().map(OrderResponse::from).toList());
}
```
Add `findByUserId` to `OrderRepository` port and `JpaOrderRepository` impl if not exists.

**Note:** If orders are served from `api` module (monolith), locate the correct controller. Recent refactors may have moved order endpoints to `order-service` — verify which module handles `/api/orders`.

**Acceptance:**
- `GET /api/orders?userId=1` returns paginated order list
- Response matches `OrderResponse` shape (id, orderNo, userId, planId, priceId, amount, currency, status, snapshotJson, createdAt, paidAt)
- Existing POST/GET-by-orderNo endpoints unchanged

### Task 1.5: ModelProvider Interface Design

**Files:**
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/ModelProvider.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/ModelCompletionRequest.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/ModelCompletionResponse.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/ModelEmbeddingRequest.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/ModelEmbeddingResponse.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/StreamChunk.java`

**Design:**
```java
public interface ModelProvider {
    String getProviderName();
    boolean supportsModel(String modelName);

    // Non-streaming chat completion
    ModelCompletionResponse chatCompletion(ModelCompletionRequest request);

    // Streaming chat completion (SSE chunks)
    Flux<StreamChunk> chatCompletionStream(ModelCompletionRequest request);

    // Embedding
    ModelEmbeddingResponse embed(ModelEmbeddingRequest request);
}

public record ModelCompletionRequest(
    String model,
    List<Message> messages,
    Double temperature,
    Integer maxTokens,
    Boolean stream,
    // ... other OpenAI-compatible params
) {
    public record Message(String role, String content) {}
}

public record ModelCompletionResponse(
    String id,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(int index, Message message, String finishReason) {}
    public record Message(String role, String content) {}
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}
}

public record StreamChunk(
    String id,
    String model,
    List<Choice> choices
) {
    public record Choice(int index, Delta delta, String finishReason) {}
    public record Delta(String role, String content) {}
}
```

**Acceptance:**
- Interface supports both streaming and non-streaming completion
- Embedding interface defined
- Object model mirrors OpenAI API shape for compatibility
- `supportsModel()` enables routing decisions

### Task 1.6: Model Routing Configuration Schema

**Files:**
- Create: `backend/api/src/main/resources/db/migration/V011__create_model_config.sql`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/config/ModelRouteConfig.java` (JPA entity)
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/config/ModelRouteRepository.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/config/ModelPricingConfig.java` (JPA entity)
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/config/ModelPricingRepository.java`

**Schema:**
```sql
-- Model/provider routes: maps model name to provider
create table ai_model_route (
    id            bigserial primary key,
    model_name    varchar(128) not null unique,  -- e.g. "gpt-4o", "claude-3-opus"
    provider      varchar(64) not null,           -- e.g. "openai", "anthropic"
    provider_model_name varchar(255) not null,     -- actual upstream model name
    model_type    varchar(32) not null default 'chat',  -- chat, embedding, image
    status        varchar(16) not null default 'active',
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

-- Per-model pricing for billing/usage cost calculation
create table ai_model_pricing (
    id              bigserial primary key,
    model_name      varchar(128) not null unique references ai_model_route(model_name),
    input_price     decimal(12,8) not null default 0,   -- per 1K tokens
    output_price    decimal(12,8) not null default 0,   -- per 1K tokens
    currency        varchar(8) not null default 'CNY',
    effective_from  timestamptz not null default now(),
    effective_to    timestamptz,                          -- null = current
    created_at      timestamptz not null default now()
);

-- Plan-to-model access mapping: which plans can access which models
create table ai_plan_model_access (
    id              bigserial primary key,
    plan_code       varchar(64) not null,                -- FK to product_plan.code
    model_name      varchar(128) not null,               -- FK to ai_model_route.model_name
    max_concurrency int not null default 5,               -- override user-level limit
    max_rpm         int not null default 0,               -- override user-level limit
    unique(plan_code, model_name)
);
```

**Acceptance:**
- Migration applies cleanly
- All 3 tables created with proper FKs and indexes
- Seed data: common models (gpt-4o, gpt-4o-mini, claude-3-haiku, claude-3-sonnet, etc.) with OpenAI as default provider
- Seed data: plan-model access mappings based on existing product_plan data

---

## Sprint 2: Subscription Fulfillment Pipeline

**Goal:** Complete the payment→fulfillment→subscription lifecycle, connect real pricing data, provide subscription management UI.

**Dependencies:** Sprint 1 Tasks 1.1-1.4 must be complete.

**Estimated effort:** 4-5 days

### Task 2.1: Extend user_subscription Table

**Files:**
- Create: `backend/order-service/src/main/resources/db/migration/V005__enhance_subscription.sql`
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/domain/model/UserSubscription.java`
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/adapter/out/persistence/SubscriptionEntity.java`
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/domain/repository/SubscriptionRepository.java` (add query methods)

**Changes:**
```sql
alter table user_subscription
    add column if not exists plan_id        bigint,
    add column if not exists price_id       bigint,
    add column if not exists order_id       bigint,
    add column if not exists amount         decimal(12,2),
    add column if not exists features_json  jsonb,        -- plan features snapshot
    add column if not exists auto_renew     boolean not null default false;
```

Add to `UserSubscription` domain model:
- `planId`, `priceId`, `orderId`, `amount`, `featuresJson`, `autoRenew` fields
- `upgrade(newPlanCode, newPriceId, newAmount)` — create pro-rated upgrade
- `downgrade(newPlanCode, newPriceId, newAmount)` — scheduled downgrade at expiry
- `cancel()` — set `autoRenew = false`, status stays active until expiry
- `suspend()` — set status to `suspended` (for non-payment)
- `reactivate()` — resume from suspended

Add repository methods:
- `findByUserIdAndPlanCode(userId, planCode)` — for upgrade check
- `findExpiringSoon(Instant before)` — for renewal notifications

**Acceptance:**
- Migration runs on existing data (nullable new columns)
- Existing subscriptions get null for new fields (backward compat)
- New subscriptions have full FK references + features snapshot

### Task 2.2: Refactor FulfillmentService

**Files:**
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/application/service/FulfillmentService.java`

**Changes:**
1. Switch from string-based `extractFromSnapshot()` to typed `OrderSnapshot` (from Task 1.3)
2. On fulfillment, update `saas_user` quota fields via Feign call to `user-service`:
   - Parse `features_json` for `quota_limit`, `concurrency`, `rpm_limit`
   - Call `PUT /api/auth/internal/users/{userId}/quota` to update user limits
3. Add upgrade/downgrade logic: check existing subscription status before creating new one
4. Add `autoRenew` support: set flag on new subscriptions

**Feign client to add in order-service:**
```java
@FeignClient(name = "user-service", path = "/api/auth/internal")
public interface UserServiceInternalClient {
    @PutMapping("/users/{userId}/quota")
    ApiResponse<Void> updateQuota(
        @PathVariable Long userId,
        @RequestBody QuotaUpdateRequest request
    );
}
```

**New endpoint in user-service:**
- `PUT /api/auth/internal/users/{userId}/quota` — internal endpoint (API-key-guarded) to update quota_limit, concurrency, rpm_limit

**Acceptance:**
- Typed snapshot used throughout fulfillment
- User quota updated on subscription activation
- Upgrade creates new subscription, extends from current expiry
- Downgrade scheduled for next billing period

### Task 2.3: Add Usage Limit Enforcement

**Files:**
- Modify: `backend/order-service/src/main/java/com/zhangyuan/order/application/service/UsageService.java`

**Changes:**
In `UsageService.recordUsage()`:
1. Look up user's active subscription via `SubscriptionRepository.findByUserIdAndActive(userId)`
2. If no active subscription → reject with 402 Payment Required error
3. Check if user has remaining quota (from `SaasUserClient.getUser(userId)` → `quotaUsed < quotaLimit`)
4. Deduct from user's quota after successful recording
5. Return rejection reason in error response

**Feign client:**
```java
@FeignClient(name = "user-service", path = "/api/auth/internal")
public interface SaasUserInternalClient {
    @GetMapping("/users/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable Long userId);
}
```

**Note:** This must NOT block the model API call itself (Sprint 3) — it's for the usage recording service. The model API auth filter (Sprint 3) handles pre-call enforcement.

**Acceptance:**
- Usage recording fails with clear error if no active subscription
- Usage recording fails if quota exceeded
- Error response includes reason and remediation hint
- Existing `POST /api/usage/record` caller (currently only seed data) may need update

### Task 2.4: Frontend Pricing Page — Real Data Connection

**Files:**
- Modify: `frontend/web/pages/pricing.vue`
- Modify: `frontend/web/pages/checkout.vue`
- Modify: `frontend/web/pages/payment.vue`

**Context:** Current `pricing.vue` already calls `GET /api/products/plans` and shows plan/price data. Checkout flow already calls `POST /api/orders` and `POST /api/payments/checkout`. Need to verify data flows end-to-end and fix any gaps.

**Changes:**
1. Verify `pricing.vue` correctly maps `GET /api/products/plans` response to UI cards (plan name, price, features, CTA button)
2. Verify `checkout.vue` passes correct `planCode` and `billingCycle` to `POST /api/orders`
3. Verify `payment.vue` poll + mock flow works end-to-end
4. Add loading states, error handling, and empty states to all 3 pages
5. Add "subscription active" badge if user already has active subscription for a plan

**Acceptance:**
- User can browse plans with real pricing data
- User can select plan → checkout → create order → pay → see subscription active
- Full end-to-end flow works from pricing page to subscription activation
- Loading/error states shown during async operations

### Task 2.5: Subscription Management UI

**Files — Web frontend (user console):**
- Create: `frontend/web/pages/dashboard/subscription.vue` — view current subscription, usage, expiry
- Modify: `frontend/web/pages/dashboard.vue` — add subscription summary card, link to subscription detail
- Modify: `frontend/web/pages/dashboard/orders.vue` — add subscription status column

**Files — Admin backend:**
- Create: `backend/order-service/src/main/java/com/zhangyuan/order/adapter/in/rest/SubscriptionAdminController.java` (enhance if exists)
- Add endpoints:
  - `POST /admin/subscriptions/{id}/cancel` — admin cancel subscription
  - `POST /admin/subscriptions/{id}/extend` — admin extend subscription
  - `GET /admin/subscriptions/active-stats` — active subscription counts

**Files — Admin frontend:**
- Modify: `frontend/admin/src/pages/orders/Subscriptions.vue` — add action buttons (cancel/extend), search/filter
- Modify: `frontend/admin/src/api/order.ts` — add subscription admin API calls

**UI Requirements (User Console):**
- Current plan display (name, status, start/expiry dates)
- Usage bar (quota used vs limit)
- Auto-renew toggle
- "Upgrade Plan" button → jump to pricing page
- "Cancel Subscription" with confirmation dialog
- Order history for this subscription

**UI Requirements (Admin):**
- Subscription list with search (by userId, planCode, status)
- Detail view: user info, plan, payment history, usage
- Actions: extend, cancel, force-fulfill
- Active subscription counts by plan (dashboard widget)

**Acceptance:**
- User can view and manage their subscription from dashboard
- Admin can view all subscriptions and perform actions
- Upgrade/downgrade flow end-to-end
- Cancellation flow (cancel → remains active until expiry)

### Task 2.6: Subscription Expiry Scheduler

**Files:**
- Create: `backend/order-service/src/main/java/com/zhangyuan/order/application/service/SubscriptionScheduler.java`

**Changes:**
```java
@Component
public class SubscriptionScheduler {
    @Scheduled(cron = "0 0 2 * * ?")  // Run at 2 AM daily
    public void checkExpiredSubscriptions() {
        // 1. Find all subscriptions with status='active' AND expires_at < now
        // 2. Mark them as 'expired'
        // 3. Update saas_user quota to default (or 0) via Feign
    }

    @Scheduled(cron = "0 0 * * * ?")  // Run hourly
    public void notifyExpiringSoon() {
        // 1. Find subscriptions expiring in next 7 days
        // 2. Log notification events (future: send email)
    }
}
```

**Acceptance:**
- Expired subscriptions automatically deactivated daily
- User quota reset to default on expiry
- Expiring-soon records flagged for notification

---

## Sprint 3: AI Model Invocation Engine

**Goal:** Build the core model calling pipeline — auth → rate limit → access control → route → call provider → record usage.

**Dependencies:** Sprint 1 Tasks 1.5-1.6 (interfaces + config schema), Sprint 2 Task 2.3 (usage enforcement).

**Estimated effort:** 5-6 days

### Task 3.1: API Key Authentication Filter

**Files:**
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/auth/ApiKeyAuthFilter.java`
- Modify: `backend/api/src/main/java/com/zhangyuan/config/SecurityConfig.java`

**Implementation:**
```java
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        String path = request.getRequestURI();
        if (!path.startsWith("/v1/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer sk-")) {
            sendError(response, 401, "Missing or invalid API key");
            return;
        }

        String apiKey = authHeader.substring(7);
        // Call user-service: GET /api/auth/verify-key?apiKey=...
        UserKeyInfo userInfo = userServiceClient.verifyApiKey(apiKey);
        if (userInfo == null) {
            sendError(response, 401, "Invalid API key");
            return;
        }

        // Set user context for downstream filters/services
        ApiKeyContext.set(userInfo);
        chain.doFilter(request, response);
    }
}
```

**Supporting:**
```java
// Thread-local context for the request
public class ApiKeyContext {
    private static final ThreadLocal<UserKeyInfo> current = new ThreadLocal<>();
    public static void set(UserKeyInfo info) { current.set(info); }
    public static UserKeyInfo get() { return current.get(); }
    public static void clear() { current.remove(); }
}

public record UserKeyInfo(
    Long userId,
    String apiKey,
    Long quotaUsed,
    Long quotaLimit,
    int concurrency,
    int rpmLimit,
    String planCode,
    String planName
) {}
```

**Note:** The existing `user-service` has `GET /api/auth/verify-key?apiKey=X` but returns boolean. Need to extend it to return full `UserKeyInfo`.

**Changes to user-service:**
- Modify `GET /api/auth/verify-key` to return `UserKeyInfo` or a new response DTO with user details
- Or create a new internal endpoint: `GET /api/auth/internal/verify-key?apiKey=X`

**SecurityConfig changes:**
- Register `ApiKeyAuthFilter` in Spring Security filter chain
- Ensure `/v1/**` paths skip the existing auth filter (Sa-Token for admin) and use API key filter instead
- `/v1/**` should be PUBLIC in Sa-Token config (handled by API key filter)

**Acceptance:**
- Request to `/v1/chat/completions` with invalid/missing key → 401
- Request with valid key → user context populated
- Request to non-`/v1/` paths unaffected
- Existing admin auth (Sa-Token) unaffected

### Task 3.2: Rate Limit Enforcement Middleware

**Files:**
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/ratelimit/RateLimiter.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/ratelimit/RateLimitFilter.java`

**Implementation:**
```java
public class RateLimiter {
    private final Cache<String, TokenBucket> buckets;  // Redis-backed cache

    public boolean tryAcquire(String userId, int cost, int rpmLimit, int concurrencyLimit) {
        String concurrencyKey = "ratelimit:concurrency:" + userId;
        String rpmKey = "ratelimit:rpm:" + userId;

        // Check concurrency (active requests)
        Long active = redisTemplate.opsForValue().increment(concurrencyKey);
        if (active > concurrencyLimit) {
            redisTemplate.opsForValue().decrement(concurrencyKey);
            return false;  // concurrency exceeded
        }

        // Check RPM via sliding window or token bucket
        if (!checkRpm(rpmKey, rpmLimit)) {
            redisTemplate.opsForValue().decrement(concurrencyKey);
            return false;  // RPM exceeded
        }

        // Store decrement callback for when request completes
        return true;
    }

    public void release(String userId) {
        redisTemplate.opsForValue().decrement("ratelimit:concurrency:" + userId);
    }
}

public class RateLimitFilter extends OncePerRequestFilter {
    // Decrements concurrency on request completion via request wrapper
}
```

**Note:** For a simpler first implementation, use a Redis-based sliding window for RPM and atomic increment/decrement for concurrency. The `RateLimitFilter` wraps the request/response to ensure `release()` is called on completion (even on errors).

**Acceptance:**
- > `concurrencyLimit` concurrent requests → 429 Too Many Requests
- > `rpmLimit` requests in a minute → 429 Too Many Requests
- Concurrency counter decremented on request completion (success or error)
- Rate limit headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

### Task 3.3: Model Access Control

**Files:**
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/access/ModelAccessService.java`

**Logic:**
```java
@Service
public class ModelAccessService {
    public void checkAccess(UserKeyInfo user, String requestedModel) {
        // 1. Get user's planCode from UserKeyInfo
        // 2. Query ai_plan_model_access WHERE plan_code = planCode AND model_name = requestedModel
        // 3. If no row → reject with 403 "model not included in your plan"
        // 4. If row exists → check concurrency/rpm overrides from the access row
    }
}
```

This is called by the model routing filter BEFORE routing the request.

**Acceptance:**
- User with plan that doesn't include requested model → 403
- User with valid plan → access granted
- Plan-level overrides applied (stricter than user defaults)

### Task 3.4: OpenAI-Compatible Provider Implementation

**Files:**
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/openai/OpenAiProvider.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/openai/OpenAiConfiguration.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/ModelRouterService.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/controller/ModelApiController.java`

**OpenAiProvider:**
```java
@Component
public class OpenAiProvider implements ModelProvider {
    private final RestTemplate restTemplate;
    private final OpenAiConfiguration config;

    @Override
    public boolean supportsModel(String modelName) {
        // Check if model is assigned to "openai" provider in ai_model_route
        return modelRouteRepository.findByModelName(modelName)
            .map(r -> "openai".equals(r.getProvider()))
            .orElse(false);
    }

    @Override
    public ModelCompletionResponse chatCompletion(ModelCompletionRequest request) {
        // Map internal request to OpenAI API format
        // Call https://api.openai.com/v1/chat/completions
        // Map response back to internal ModelCompletionResponse
    }

    @Override
    public Flux<StreamChunk> chatCompletionStream(ModelCompletionRequest request) {
        // SSE streaming via WebClient
        // Parse each "data: ..." line into StreamChunk
    }
}
```

**OpenAiConfiguration:**
```java
@Configuration
@ConfigurationProperties(prefix = "ai.provider.openai")
public class OpenAiConfiguration {
    private String apiKey;        // from SystemSetting or config
    private String baseUrl;       // "https://api.openai.com/v1"
    private int connectTimeout;
    private int readTimeout;
}
```

**ModelRouterService:**
```java
@Service
public class ModelRouterService {
    private final List<ModelProvider> providers;
    private final ModelRouteRepository routeRepository;

    public ModelProvider routeModel(String modelName) {
        // Look up which provider handles this model
        ModelRoute route = routeRepository.findByModelName(modelName)
            .orElseThrow(() -> new ModelNotFoundException(modelName));
        // Find the provider bean
        return providers.stream()
            .filter(p -> p.getProviderName().equals(route.getProvider()))
            .findFirst()
            .orElseThrow(() -> new ProviderNotFoundException(route.getProvider()));
    }
}
```

**ModelApiController:**
```java
@RestController
@RequestMapping("/v1")
public class ModelApiController {

    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletion(@RequestBody @Valid ModelCompletionRequest request) {
        // 1. Authenticate (ApiKeyAuthFilter already did this)
        UserKeyInfo user = ApiKeyContext.get();

        // 2. Check access (plan→model)
        accessService.checkAccess(user, request.model());

        // 3. Route to correct provider
        ModelProvider provider = router.routeModel(request.model());

        // 4. Check rate limits
        if (!rateLimiter.tryAcquire(user.userId(), 1, user.rpmLimit(), user.concurrency())) {
            return ResponseEntity.status(429).body(new ErrorResponse("rate_limit_exceeded", "..."));
        }

        try {
            if (request.stream()) {
                // Streaming response
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(provider.chatCompletionStream(request));
            } else {
                // Non-streaming
                ModelCompletionResponse response = provider.chatCompletion(request);
                return ResponseEntity.ok(response);
            }
        } finally {
            // 5. Record usage asynchronously
            usageService.recordUsageAsync(user, request, response);
            rateLimiter.release(user.userId());
        }
    }
}
```

**Acceptance:**
- `POST /v1/chat/completions` with valid key → response from upstream AI provider
- Streaming (SSE) and non-streaming both work
- Invalid model → 404 error
- Plan restriction → 403 error
- Rate exceeded → 429 error
- Usage recorded after successful call (token count from provider response)
- Proxy all standard OpenAI-compatible params: model, messages, temperature, max_tokens, stream, etc.

### Task 3.5: Embedding Provider + Endpoint

**Files:**
- Create/Modify: `backend/api/src/main/java/com/zhangyuan/modules/ai/provider/openai/OpenAiProvider.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/controller/EmbeddingController.java`

Extend `OpenAiProvider` with:
```java
@Override
public ModelEmbeddingResponse embed(ModelEmbeddingRequest request) {
    // POST https://api.openai.com/v1/embeddings
    // Map response
}
```

Add endpoint:
```java
@PostMapping("/v1/embeddings")
public ResponseEntity<ModelEmbeddingResponse> embed(@RequestBody ModelEmbeddingRequest request) {
    // Same auth/access/rate-limit flow as chat completions
}
```

**Acceptance:**
- `POST /v1/embeddings` returns embeddings from configured provider
- Token count tracked in usage records

### Task 3.6: Model Configuration Admin UI

**Files — Admin frontend:**
- Create: `frontend/admin/src/pages/system/ModelRoutes.vue` — model route management
- Create: `frontend/admin/src/pages/system/ModelPricing.vue` — per-model pricing
- Create: `frontend/admin/src/pages/system/PlanModelAccess.vue` — plan→model mapping
- Modify: `frontend/admin/src/router/index.ts` — add routes

**Files — Admin backend (api module):**
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/admin/ModelRouteAdminController.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/admin/ModelPricingAdminController.java`
- Create: `backend/api/src/main/java/com/zhangyuan/modules/ai/admin/PlanModelAccessAdminController.java`

**Endpoints:**
| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| GET | `/admin/ai/models` | `ai:model:list` | List model routes |
| POST | `/admin/ai/models` | `ai:model:create` | Add model route |
| PUT | `/admin/ai/models/{id}` | `ai:model:update` | Update model route |
| DELETE | `/admin/ai/models/{id}` | `ai:model:delete` | Remove model route |
| GET | `/admin/ai/pricing` | `ai:pricing:list` | List model pricing |
| POST | `/admin/ai/pricing` | `ai:pricing:create` | Add pricing entry |
| GET | `/admin/ai/plan-access` | `ai:access:list` | List plan-model mappings |
| POST | `/admin/ai/plan-access` | `ai:access:create` | Add plan-model mapping |
| DELETE | `/admin/ai/plan-access/{id}` | `ai:access:delete` | Remove plan-model mapping |

**Seed data permissions (V011 or V012 migration):**
```sql
INSERT INTO admin_permission (code, name, module) VALUES
('ai:model:list', '查看模型路由', 'ai'),
('ai:model:create', '创建模型路由', 'ai'),
('ai:model:update', '更新模型路由', 'ai'),
('ai:model:delete', '删除模型路由', 'ai'),
('ai:pricing:list', '查看模型定价', 'ai'),
('ai:pricing:create', '创建模型定价', 'ai'),
('ai:access:list', '查看模型访问权限', 'ai'),
('ai:access:create', '创建模型访问权限', 'ai'),
('ai:access:delete', '删除模型访问权限', 'ai');
```

**Acceptance:**
- Admin can add/remove/edit model routes through UI
- Admin can configure per-model pricing
- Admin can map plans to allowed models
- Changes take effect immediately (no restart)

### Task 3.7: Seed Data for Models + Pricing

**Files:**
- Modify: `backend/tools/seed-data/SeedDataGenerator.java` or create new SQL migration

**Seed data:**
```sql
-- Model routes (using SystemSetting or direct ai_model_route insert)
INSERT INTO ai_model_route (model_name, provider, provider_model_name, model_type) VALUES
('gpt-4o', 'openai', 'gpt-4o', 'chat'),
('gpt-4o-mini', 'openai', 'gpt-4o-mini', 'chat'),
('gpt-4-turbo', 'openai', 'gpt-4-turbo', 'chat'),
('claude-3-haiku', 'openai', 'claude-3-haiku', 'chat'),   -- if proxied via OpenAI-compatible
('text-embedding-3-small', 'openai', 'text-embedding-3-small', 'embedding');

-- Pricing (example values, to be configured by admin)
INSERT INTO ai_model_pricing (model_name, input_price, output_price) VALUES
('gpt-4o', 5.00, 15.00),           -- ¥ per 1M tokens (example)
('gpt-4o-mini', 0.15, 0.60),
('text-embedding-3-small', 0.02, 0.02);

-- Plan-model access (map existing plans to models)
INSERT INTO ai_plan_model_access (plan_code, model_name) VALUES
('free', 'gpt-4o-mini'),
('pro', 'gpt-4o-mini'),
('pro', 'gpt-4o'),
('pro', 'text-embedding-3-small');
```

**Acceptance:**
- After seed data, admin UI shows pre-populated models and pricing
- Plans already have model access configured
- Default OpenAI API key stored in SystemSetting

---

## Dependency Graph

```
Sprint 1                    Sprint 2                    Sprint 3
───────                     ───────                     ───────
1.1 Fix callback ─────┐
1.2 Add fulfilled_at ──┤
1.3 Typed snapshot ────┼────→ 2.2 Refactor Fulfillment ─┐
1.4 Add orders list ───┤                                │
                       │    2.1 Extend subscription ────┤
1.5 ModelProvider ─────┤                                │
1.6 Config schema ─────┤    2.3 Usage enforcement ──────┼──→ 3.3 Access control
                       │    2.4 Pricing page data ──────┤    │
                       │    2.5 Subscription UI ────────┤    │
                       │    2.6 Expiry scheduler ───────┤    │
                       │                                │    │
                       │                                ├──→ 3.1 Auth filter
                       │                                ├──→ 3.2 Rate limiter
                       │                                ├──→ 3.4 Model API
                       │                                ├──→ 3.5 Embedding
                       │                                └──→ 3.6 Admin UI
                       │                                     3.7 Seed data
```

**Parallel execution opportunities:**
- Sprint 1: Tasks 1.1–1.4 can run in parallel (different services/files)
- Sprint 1: Task 1.5–1.6 can run in parallel with 1.1–1.4
- Sprint 2: Task 2.1, 2.4, 2.6 can run in parallel
- Sprint 2: Task 2.2 depends on 1.3 + 2.1
- Sprint 2: Task 2.3 can start after 2.2
- Sprint 2: Task 2.5 can start after 2.1 + 2.4
- Sprint 3: Tasks 3.1, 3.2, 3.6, 3.7 can run in parallel
- Sprint 3: Task 3.3 depends on 3.1 + 1.6
- Sprint 3: Task 3.4 depends on 1.5 + 3.1 + 3.2 + 3.3
- Sprint 3: Task 3.5 depends on 3.4 (same provider)

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| OpenAI API key exposure in config | Medium | High | Use environment variables, not hardcoded; add secret scanning |
| Dual `order_main` tables (api + order-service) cause confusion | High | Medium | Document ownership clearly; new code only touches order-service |
| Breaking existing subscription data during migration | Low | High | All new columns are nullable; existing subscriptions unaffected |
| Rate limiter becomes bottleneck | Low | Medium | Redis-based, async decrement; start simple, optimize later |
| Streaming response complicates usage tracking | Medium | Medium | Count tokens on stream completion from final chunk; approximate if needed |
| Frontend-backend contract mismatch on order list API | High | Medium | Task 1.4 specifically addresses this gap |

---

## Rollback Plan

| Change | Rollback Strategy |
|--------|-------------------|
| DB migrations | Each migration has a corresponding `-- down` comment for manual rollback; Flyway `undo` not enabled, so roll forward with new migration |
| New controllers/endpoints | No rollback needed — additive change |
| Auth filter for `/v1/*` | Disable by removing filter registration in SecurityConfig; existing admin auth unaffected |
| Rate limiter | Disable `RateLimitFilter`; system continues without rate limiting |

---

## Test Strategy

| Layer | Focus | Tool |
|-------|-------|------|
| Unit | Domain logic: subscription state transitions, OrderSnapshot parsing, rate limiter logic | JUnit 5 |
| Integration | Controllers: request→response validation, auth filter, Feign client mocking | @SpringBootTest + MockMvc |
| E2E | Full checkout→payment→fulfillment flow, model API call → usage recorded | curl + bash (existing e2e-http-test.sh pattern) |
| Frontend | Pricing page data rendering, subscription UI, error/loading states | vue-tsc type check + manual |
