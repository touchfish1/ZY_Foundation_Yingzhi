# Security Audit Report — ZY_Foundation_Yingzhi

**Audit Date:** 2026-06-10
**Scope:** Full codebase (backend microservices + frontend)
**Type:** Manual static analysis (read-only)
**Auditor:** Sisyphus-Jr automated security audit

---

## Summary

A comprehensive security audit was conducted across the entire ZY_Foundation_Yingzhi codebase.  
The audit inspected **backend microservices** (api, auth-service, user-service, order-service, payment-service, system-service, ai-service, gateway) and **frontend applications** (admin Vue 3 + web Nuxt 3).

**Risk Breakdown:**

| Severity | Count |
|----------|-------|
| CRITICAL | 4 |
| HIGH     | 8 |
| MEDIUM   | 6 |
| LOW      | 4 |
| **Total** | **22** |

**Most Critical Issues:**
1. Hardcoded default secrets and credentials in production configuration files
2. Inconsistent Sa-Token authentication across microservices — most `/api/**` endpoints are publicly accessible
3. Missing auth on order-fulfill and payment-mock endpoints
4. CORS misconfiguration with credential-bearing wildcard origins

---

## Methodology

1. **Static Code Search** — Grep/ripgrep for vulnerability patterns across ~200k lines of code
2. **Configuration Review** — All 18 `application.yml` files, gateway routes, Sa-Token configs
3. **Authentication Flow Analysis** — Each service's SaTokenConfig, SecurityConfig, ApiKeyAuthFilter
4. **Error Handling Review** — Global exception handlers for information leakage
5. **Dependency Review** — build.gradle files for dependency versions
6. **Frontend Review** — Token storage, CORS, API client patterns

---

## Findings

### CRITICAL

#### C-1: Hardcoded Default JWT Secret in Configuration Files

**Affected Files:**
- `backend/api/src/main/resources/application.yml` (line 52)
- `backend/auth-service/src/main/resources/application.yml` (line 52)
- `backend/api/src/test/resources/application-test.yml` (line 16)

**Description:**
The JWT secret defaults to `change-this-development-secret-change-before-production` in production configuration files. If this default is not overridden via the `JWT_SECRET` environment variable, an attacker can forge arbitrary JWT tokens to impersonate any user.

```yaml
jwt-secret: ${JWT_SECRET:change-this-development-secret-change-before-production}
```

**Impact:**
CRITICAL — Complete authentication bypass. Attacker can forge admin tokens, create arbitrary users, modify system settings, access all data.

**Fix:**
- Remove the default value: `jwt-secret: ${JWT_SECRET}`
- Generate a strong random secret (minimum 256-bit) and set `JWT_SECRET` in production environment
- Rotate the key immediately if the default was ever used in a production-accessible environment

---

#### C-2: Hardcoded Default Admin Password

**Affected Files:**
- `backend/api/src/main/resources/application.yml` (line 56)
- `backend/auth-service/src/main/resources/application.yml` (line 56)

**Description:**
The default admin password `admin123` is embedded in production configuration files. This password is trivial to guess and is documented in the README.

```yaml
password: ${DEFAULT_ADMIN_PASSWORD:admin123}
```

**Impact:**
CRITICAL — If not overridden, any actor who gains network access can log in as a super-admin with a well-known password.

**Fix:**
- Remove the default: `password: ${DEFAULT_ADMIN_PASSWORD}`
- Enforce a strong password policy via environment variable
- Consider implementing initial admin password rotation on first login

---

#### C-3: Hardcoded Database Password in Multiple Production Configs

**Affected Files:**
- `backend/api/src/main/resources/application.yml` (line 24)
- `backend/auth-service/src/main/resources/application.yml` (line 26)
- `backend/user-service/src/main/resources/application.yml` (line 24)
- `backend/order-service/src/main/resources/application.yml` (line 36)
- `backend/payment-service/src/main/resources/application.yml` (line 38)
- `backend/system-service/src/main/resources/application.yml` (line 31)
- `backend/api/src/main/resources/application-wsl.yml` (line 5)

**Description:**
Database password defaults to `zhangyuan` in all production YAML files. Also, the MinIO secret key defaults to `zhangyuan-minio-secret` in the api module:

```yaml
password: ${DB_PASSWORD:zhangyuan}
secret-key: ${MINIO_SECRET_KEY:zhangyuan-minio-secret}
```

**Impact:**
CRITICAL — If the database can be reached from the network (default config points to `100.125.148.23`), the entire data store is at risk.

**Fix:**
- Remove default values from config files
- Use environment-specific secrets management (Vault, AWS Secrets Manager, or K8s Secrets)
- Rotate the database password and MinIO credentials immediately

---

#### C-4: Hardcoded Nacos Passwords in All Service Configs

**Affected Files:**
All 8 microservice `application.yml` files contain Nacos credentials, with most using the default:

```yaml
password: ${NACOS_PASSWORD:chengccn}
```

**Description:**
The Nacos configuration center password `chengccn` is hardcoded across all services. Nacos holds configuration data including datasource credentials.

**Impact:**
CRITICAL — Compromise of Nacos means compromise of all service configurations.

**Fix:**
- Remove the default password value
- Use a strong Nacos admin password
- Enable Nacos authentication and access control lists

---

### HIGH

#### H-1: Inconsistent Sa-Token Route Protection — `/api/**` Largely Unauthenticated

**Affected Files:**
- `backend/api/src/main/java/com/zhangyuan/common/security/SaTokenConfig.java` (line 32)
- `backend/auth-service/src/main/java/com/zhangyuan/auth/common/security/SaTokenConfig.java` (line 29)
- `backend/payment-service/src/main/java/com/zhangyuan/payment/common/SaTokenConfig.java` (line 16)
- `backend/order-service/src/main/java/com/zhangyuan/order/common/SaTokenConfig.java` (line 18)

**Description:**
The Sa-Token interceptor configuration across 4 of 7 services explicitly permits all `/api/**` routes without authentication:

```java
SaRouter.match("/api/**", "/actuator/**").check(r -> {});
```

This means ALL public API endpoints (orders, payments, CMS, products, assets, etc.) are accessible without any authentication in these services. Only `user-service` and `system-service` properly protect their `/api/` endpoints.

**Contrast — user-service (correct):**
```java
SaRouter.match("/api/**")
    .notMatch("/api/auth/**")
    .check(r -> StpUtil.checkLogin());
```

**Impact:**
HIGH — Unauthenticated access to order creation, payment checkout, CMS content (admin-level), product management, asset management, and system settings.

**Fix:**
- Standardize route protection across ALL services to align with the user-service pattern
- `/api/**` should require authentication by default, with explicit exclusions only for genuinely public endpoints (`/api/auth/login`, `/api/auth/register`, `/api/system/ping`, etc.)
- Add path-based permission checks (e.g., `@SaCheckPermission`) on admin endpoints

---

#### H-2: `@SaIgnore` on Order and Payment Controllers (Bypasses Sa-Token)

**Affected Files:**
- `backend/api/src/main/java/com/zhangyuan/modules/order/adapter/in/rest/OrderPublicController.java` (line 26)
- `backend/api/src/main/java/com/zhangyuan/modules/payment/adapter/in/rest/PaymentPublicController.java` (line 23)

**Description:**
Both controllers use the `@SaIgnore` annotation which explicitly bypasses Sa-Token authentication:

```java
@SaIgnore
@RequestMapping("/api/orders")
public class OrderPublicController { ... }
```

While this might be intentional for truly public endpoints, the `OrderPublicController.create()` method still accepts `userId = null` for non-logged-in users, creating orders without any user context. The `PaymentPublicController` also has no auth check.

**Impact:**
HIGH — Anyone can create orders and initiate payment checkouts without authentication. Combined with H-4 (mock endpoint exposure), this enables fraudulent order creation and payment simulation.

**Fix:**
- Remove `@SaIgnore` from controllers and rely on the service-level Sa-Token configuration
- If certain endpoints must be public, annotate individual methods rather than the whole controller
- Order creation requires a valid user context — enforce authentication
- Payment checkout requires a valid order context — enforce authentication

---

#### H-3: Order Fulfillment Endpoint Has No Authentication

**Affected Files:**
- `backend/order-service/src/main/java/com/zhangyuan/order/common/ApiKeyAuthFilter.java` (line 35)
- `backend/order-service/src/main/java/com/zhangyuan/order/adapter/in/rest/OrderPublicController.java` (lines 63-68)

**Description:**
The `ApiKeyAuthFilter` explicitly skips authentication for the fulfill endpoint:

```java
if (!path.startsWith("/api/orders") || path.contains("fulfill")) {
    // skip auth
    return;
}
```

The fulfill endpoint at `POST /api/orders/{orderNo}/fulfill` is callable without any authentication (no Sa-Token, no API key check).

**Impact:**
HIGH — Any unauthenticated actor can fulfill orders, triggering subscription activations and quota allocations without payment. This is a direct financial impact vulnerability.

**Fix:**
- Remove the `path.contains("fulfill")` bypass from the auth filter
- If fulfill must be called internally from payment-service, use a service-to-service auth mechanism (e.g., internal JWT, mTLS, or a shared secret header)
- At minimum, require a valid Sa-Token session with appropriate permissions

---

#### H-4: Payment Mock Endpoint Unprotected in Dev/Default Profile

**Affected Files:**
- `backend/payment-service/src/main/java/com/zhangyuan/payment/common/SecurityConfig.java` (lines 25-31)
- `backend/payment-service/src/main/java/com/zhangyuan/payment/adapter/in/rest/PaymentPublicController.java` (lines 28-31)
- `backend/api/src/main/java/com/zhangyuan/modules/payment/adapter/in/rest/PaymentPublicController.java` (lines 49-54)

**Description:**
The payment mock endpoint `POST /api/payments/mock/{paymentNo}/success` is used for testing but is unprotected outside the `prod` profile. The prod profile denies it, but the default and dev profiles allow all requests:

```java
@Bean
@Profile({"dev", "test", "default"})
public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
}
```

**Impact:**
HIGH — If a dev/staging environment is network-accessible, an attacker can simulate successful payments without actually paying. Combined with H-2, this is a full payment bypass.

**Fix:**
- Remove the mock endpoint from production builds entirely
- Gate mock endpoints behind a feature flag that cannot be enabled in production
- Add authentication even to mock endpoints
- Consider using a separate mock service that is never deployed alongside production

---

#### H-5: Hibernate `ddl-auto: update` in Production-Targeted Configs

**Affected Files:**
All microservices except `api` use `ddl-auto: update`:

| Service | ddl-auto | Status |
|---------|----------|--------|
| api | `validate` | ✅ Correct |
| auth-service | `update` | ❌ |
| user-service | `update` | ❌ |
| order-service | `update` | ❌ |
| payment-service | `update` | ❌ |
| system-service | `update` | ❌ |

**Description:**
Hibernate's `ddl-auto: update` allows automatic schema modification at runtime. An attacker who gains any form of database access (or can exploit JPA injection) can alter the database schema arbitrarily.

**Impact:**
HIGH — Schema tampering, privilege escalation (e.g., adding admin users directly), data destruction.

**Fix:**
- Change all services to `ddl-auto: validate`
- Use Flyway migrations (as the `api` module does correctly) for all schema changes
- Remove `spring.jpa.hibernate.ddl-auto` from production configs entirely

---

#### H-6: Gateway CORS Misconfiguration — Credential-Bearing Wildcard Origins

**Affected Files:**
- `backend/gateway/src/main/resources/application.yml` (lines 21-35)

**Description:**
The gateway configures `allowedOriginPatterns` with wildcards (`http://localhost:*`, `http://127.0.0.1:*`, `https://*.zhangyuan.example.com`) while also setting `allowCredentials: true`:

```yaml
allowedOriginPatterns:
  - "http://localhost:*"
  - "http://127.0.0.1:*"
  - "https://*.zhangyuan.example.com"
allowCredentials: true
```

**Impact:**
HIGH — While not the most permissive (doesn't allow `*` with credentials), the patterns are broad. Any subdomain of `zhangyuan.example.com` (including XSS-vulnerable ones) can make credentialed requests. Additionally, any attacker on localhost can make requests with credentials.

**Fix:**
- Restrict allowed origins to specific, known values only
- Avoid wildcard subdomain patterns if subdomains are not independently hardened
- Remove `http://localhost:*` and `http://127.0.0.1:*` from production CORS config

---

#### H-7: API Key Auth Bypass via Missing / Missing/Empty Rate Limiting

**Affected Files:**
- `backend/ai-service/src/main/java/com/zhangyuan/ai/common/RateLimitFilter.java` (lines 57-60)

**Description:**
When `resolveUserId()` fails (returns null), the rate limit filter passes the request through without any rate limiting:

```java
if (userId == null) {
    chain.doFilter(request, response);
    return;
}
```

An attacker can send requests without a valid Authorization header, and these requests bypass rate limiting entirely. While auth is still checked at the controller level, the lack of globally-applied rate limiting allows for brute-force attacks on the API key.

**Impact:**
HIGH — API key brute-forcing, resource exhaustion without authentication, bypassing rate limit quotas.

**Fix:**
- Apply IP-based rate limiting as a fallback when userId cannot be resolved
- Consider a global rate limit that applies even to unauthenticated requests
- Log and alert on high volumes of requests with invalid API keys

---

#### H-8: Balance and Transaction Endpoints Expose Any User's Data

**Affected Files:**
- `backend/user-service/src/main/java/com/zhangyuan/user/adapter/in/rest/BalanceController.java` (lines 24-36)

**Description:**
The balance controller exposes user financial data via direct userId path parameters without ownership verification:

```java
@GetMapping("/{userId}")
public ApiResponse<BalanceResponse> getBalance(@PathVariable Long userId) {
    return ApiResponse.ok(new BalanceResponse(userId, balanceService.getBalance(userId)));
}
```

Any authenticated user (or unauthenticated — see H-1) can query any other user's balance and transaction history by simply changing the userId.

**Impact:**
HIGH — Unauthorized access to financial data (balance, transaction history) of all users.

**Fix:**
- Verify that the requesting user owns the requested userId (compare `StpUtil.getLoginIdAsLong() == userId`)
- For admin access, check `@SaCheckPermission` annotation
- Add proper authorization to the balance and transaction endpoints

---

### MEDIUM

#### M-1: Information Disclosure via Exception Messages

**Affected Files:**
- `backend/api/src/main/java/com/zhangyuan/common/security/GlobalExceptionHandler.java` (lines 51-55)
- `backend/order-service/src/main/java/com/zhangyuan/order/common/GlobalExceptionHandler.java` (lines 35-39)
- `backend/payment-service/src/main/java/com/zhangyuan/payment/common/GlobalExceptionHandler.java` (lines 25-29)
- `backend/user-service/src/main/java/com/zhangyuan/user/adapter/in/rest/BalanceController.java` (line 46)
- `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminSystemUserController.java` (lines 68, 82)
- `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminPermissionController.java` (lines 66, 81)
- `backend/ai-service/src/main/java/com/zhangyuan/ai/adapter/in/rest/ChatProxyController.java` (lines 42-51)
- `backend/ai-service/src/main/java/com/zhangyuan/ai/adapter/in/rest/EmbeddingController.java` (lines 80-86)

**Description:**
Multiple exception handlers return `e.getMessage()` directly to the API caller. These messages can leak internal details such as:
- Whether an email is already registered
- Why a permission creation failed (DB constraint details)
- System state information

**Impact:**
MEDIUM — Information leakage that aids attackers in reconnaissance.

**Fix:**
- Map exceptions to user-safe messages in all handlers
- Log the full exception internally, return only a generic message
- For validation errors, return only the field-level error, not the root cause

---

#### M-2: In-Memory Rate Limiter Bypass via IP Spoofing

**Affected Files:**
- `backend/user-service/src/main/java/com/zhangyuan/user/adapter/in/rest/SaasUserController.java` (lines 27-35, 41-46)

**Description:**
The user-service rate limiter is based on `request.getRemoteAddr()` which can be spoofed via `X-Forwarded-For` headers if behind a proxy. Also, the rate limit is only 100ms between requests, which is a very permissive rate:

```java
private boolean isRateLimited(String ip) {
    long now = System.currentTimeMillis();
    AtomicLong lastAccess = lastAccessMap.computeIfAbsent(ip, k -> new AtomicLong(0));
    long last = lastAccess.get();
    if (now - last < RATE_LIMIT_PER_MILLIS) {
        return true;
    }
    return !lastAccess.compareAndSet(last, now);
}
```

**Impact:**
MEDIUM — An attacker can bypass rate limiting with IP rotation or spoofing, enabling brute-force attacks on login/register/verify-key endpoints.

**Fix:**
- Use a Redis-backed rate limiter (like the ai-service) instead of in-memory maps
- Respect `X-Forwarded-For` headers when behind a proxy
- Implement exponential backoff on failed login attempts per email address

---

#### M-3: Actuator Endpoints Exposed Without Auth

**Affected Files:**
All services expose `/actuator/**` without authentication (via Sa-Token config):

```java
SaRouter.match("/api/**", "/actuator/**").check(r -> {});
```

While only `health,info,metrics` are exposed (reasonable), the `info` endpoint can leak build info, git details, and environment metadata.

**Impact:**
MEDIUM — Information disclosure (build info, git commit, Java version). Attackers can identify vulnerable dependency versions.

**Fix:**
- Restrict actuator exposure to internal networks only
- Add authentication for actuator endpoints in production
- Disable `info` exposure or sanitize the info endpoint output

---

#### M-4: No CSRF Protection on Any Service

**Affected Files:**
All 8 microservice `SecurityConfig.java` files:

```java
http.csrf(AbstractHttpConfigurer::disable)
```

**Description:**
CSRF protection is disabled across all services. While this is common for stateless REST APIs, any endpoint that relies on cookie-based authentication (Sa-Token sessions) could be vulnerable to CSRF attacks.

**Impact:**
MEDIUM — If a user is logged in (browser cookie for admin panel), an external site can trigger state-changing requests on their behalf.

**Fix:**
- Evaluate whether any endpoints use cookie-based auth (Sa-Token does use cookies by default)
- If yes, implement CSRF tokens or SameSite=Strict cookie policy
- If using only Bearer tokens (not cookies) from browsers, CSRF risk is mitigated but confirm this

---

#### M-5: Missing SaTokenConfig in AI-Service

**Affected Files:**
`backend/ai-service/` — No `SaTokenConfig.java` exists

**Description:**
The ai-service has no Sa-Token interceptor configuration. While it doesn't have `/admin/` routes, it also has no route-level authentication enforcement. Authentication is handled at the controller level instead.

**Impact:**
MEDIUM — If an `/admin/` or other privileged route is added to the ai-service, it would be unprotected. Currently, the `/v1/models` endpoint is entirely unauthenticated (returns model list without any auth check).

**Fix:**
- Add a consistent SaTokenConfig to the ai-service
- Consider adding authentication to the `/v1/models` endpoint or at least rate-limit it

---

#### M-6: Order List Endpoint Returns All Orders Without Auth

**Affected Files:**
- `backend/order-service/src/main/java/com/zhangyuan/order/adapter/in/rest/OrderPublicController.java` (lines 36-41)

**Description:**
The `GET /api/orders` endpoint (with `@SaIgnore` and exposed to `/api/**` which has no auth in order-service) lists orders. When `userId` is null, it returns ALL orders:

```java
@GetMapping
public ApiResponse<List<OrderResponse>> list(@RequestParam(required = false) Long userId) {
    if (userId != null) {
        return ApiResponse.ok(orderApplicationService.listOrdersByUserId(userId));
    }
    return ApiResponse.ok(orderApplicationService.listOrders());
}
```

**Impact:**
MEDIUM — Any unauthenticated user can enumerate all orders in the system, learning about customers, pricing, and order volumes.

**Fix:**
- Require authentication for order listing
- Never allow returning all orders without authorization
- Scope results to the authenticated user by default

---

### LOW

#### L-1: Frontend Tokens Stored in localStorage (XSS-Vulnerable)

**Affected Files:**
- `frontend/admin/src/api/http.ts` (line 1-10)
- `frontend/web/composables/useSaasAuth.ts` (lines 21-47, 77, 92-96)

**Description:**
Both frontend applications store authentication tokens in `localStorage`, which is accessible via JavaScript. Any XSS vulnerability can exfiltrate tokens.

**Impact:**
LOW — Token exfiltration requires an XSS vulnerability to exploit. Acceptable risk for many applications, but can be mitigated.

**Fix:**
- Use HttpOnly cookies for token storage when possible
- For Nuxt: leverage `useCookie()` with `httpOnly: true` (requires SSR)
- Implement Content Security Policy headers to limit XSS damage

---

#### L-2: Missing Security Headers

**Description:**
No security-related HTTP headers detected: no `Content-Security-Policy`, `X-Content-Type-Options`, `Strict-Transport-Security`, `X-Frame-Options`, or `Referrer-Policy`.

**Impact:**
LOW — Increases attack surface for XSS, clickjacking, MIME-type confusion.

**Fix:**
- Add security headers via gateway filter or Spring Security configuration:
  - `Content-Security-Policy`
  - `X-Content-Type-Options: nosniff`
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
  - `X-Frame-Options: DENY`
  - `Referrer-Policy: strict-origin-when-cross-origin`

---

#### L-3: Order-Service RPM Rate Limiter Uses Incorrect Sliding Window Implementation

**Affected Files:**
- `backend/order-service/src/main/java/com/zhangyuan/order/common/ApiKeyAuthFilter.java` (lines 117-129)

**Description:**
The SlidingWindowCounter resets its count when elapsed > 60s on the **next request**, not continuously. If there are no requests for 61s, the counter resets to 1 but the windowStart is NOT updated, so the next request within 60s will not trigger a reset properly.

```java
long incrementAndGet() {
    long elapsed = System.currentTimeMillis() - windowStart;
    if (elapsed > 60_000) {
        count.set(1);  // Resets to 1, but windowStart is stale!
        return 1;
    }
    return count.incrementAndGet();
}
```

**Impact:**
LOW — Rate limiting can be slightly exceeded after idle periods. Not easily exploitable but functionally incorrect.

**Fix:**
- Update `windowStart` along with the count reset
- Consider using a proper sliding window algorithm or Redis-backed rate limiting

---

#### L-4: Gateway Proxies Web Frontend Via Catch-All Route

**Affected Files:**
- `backend/gateway/src/main/resources/application.yml` (lines 180-183)

**Description:**
The gateway has a catch-all route (`Path=/**`) that proxies to the web frontend. This means the gateway will serve the frontend for any unmatched path, potentially masking routing issues.

**Impact:**
LOW — Minimal security impact; primarily an operational concern.

**Fix:**
- Consider explicit route definitions instead of a catch-all
- Ensure security headers are applied at the gateway level

---

## Recommended Remediation Priorities

### Immediate (Week 1)
| Priority | Issue | Effort |
|----------|-------|--------|
| P0 | C-1: Default JWT secret | 1 hour |
| P0 | C-2: Default admin password | 1 hour |
| P1 | C-3: Default database password | 2 hours |
| P1 | C-4: Default Nacos password | 2 hours |

### Short-term (Week 2-3)
| Priority | Issue | Effort |
|----------|-------|--------|
| P1 | H-1: Standardize Sa-Token route protection | 2-3 days |
| P1 | H-3: Order fulfillment auth bypass | 1 day |
| P1 | H-4: Payment mock endpoint exposure | 1 day |
| P1 | H-8: Balance endpoint authorization | 1 day |
| P2 | H-5: Hibernate ddl-auto: validate | 1 day |

### Medium-term (Week 4-6)
| Priority | Issue | Effort |
|----------|-------|--------|
| P2 | H-2: Remove @SaIgnore from controllers | 2 days |
| P2 | H-6: Tighten CORS configuration | 1 day |
| P2 | H-7: Rate limiting for unauthenticated requests | 1 day |
| P3 | M-1: Information disclosure via exceptions | 2 days |
| P3 | M-2: Redis-backed rate limiter | 1 day |
| P3 | M-6: Order listing authorization | 1 day |

### Long-term (Month 2+)
| Priority | Issue | Effort |
|----------|-------|--------|
| P3 | M-3: Actuator protection | 1 day |
| P3 | M-4: CSRF evaluation/implementation | 2 days |
| P3 | M-5: AI service SaTokenConfig | 1 day |
| P4 | L-1 to L-4: Low-priority hardening | 3 days |

---

## Follow-up Actions

1. **Environment Variable Audit:**
   - Verify that `JWT_SECRET`, `DEFAULT_ADMIN_PASSWORD`, `DB_PASSWORD`, `MINIO_SECRET_KEY`, and `NACOS_PASSWORD` are all overridden in production environments
   - Audit CI/CD pipelines for any secret leakage

2. **Penetration Testing:**
   - Conduct automated API security scanning (ZAP, Burp Suite) against staging environment
   - Test authentication bypass scenarios identified in this report

3. **Security Monitoring:**
   - Add security-relevant logging
   - Monitor for unauthorized access attempts to protected endpoints
   - Set up alerts for rate limit violations and authentication failures

4. **Dependency Updates:**
   - Keep Spring Boot (3.3.5 → latest 3.3.x patch) and all dependencies updated
   - Enable Dependabot or similar for automated dependency vulnerability alerts
   - Sa-Token 1.39.0 — check for latest security patches

5. **Security Standards Implementation:**
   - Consider adopting OWASP ASVS (Application Security Verification Standard)
   - Implement automated SAST/DAST in CI pipeline
   - Add security-focused code review checklist

6. **Authentication Architecture Improvements:**
   - Implement service-to-service authentication (internal JWT or mTLS)
   - Replace per-service Sa-Token duplication with centralized auth at gateway
   - Consider OAuth2 / OIDC for external API authentication

---

*Report generated by Sisyphus-Jr automated security audit tool. All findings based on static code analysis. Manual verification recommended for all HIGH and CRITICAL items before remediation.*
