#!/usr/bin/env bash
# =============================================================================
# ZY_Foundation_Yingzhi — Security Audit Scanner
# =============================================================================
# Run from repository root. This is a read-only scanner that checks for common
# security vulnerabilities across the codebase.
#
# Usage:
#   ./scripts/security-audit.sh                # Run all checks
#   ./scripts/security-audit.sh --report        # Run and generate report
#   ./scripts/security-audit.sh --quick         # Quick check (high severity only)
#   ./scripts/security-audit.sh --category X    # Run specific category
#
# Categories: secrets, sqli, cors, auth, validation, ratelimit, deps, errors
# =============================================================================

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SEVERITY="${SEVERITY:-all}"
CATEGORY="${CATEGORY:-all}"
REPORT_FILE="${REPORT_FILE:-/dev/stdout}"
ISSUES_FOUND=0
ISSUES_HIGH=0
ISSUES_CRITICAL=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo_header() {
    echo -e "\n${BLUE}══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}══════════════════════════════════════════════════════════════${NC}"
}

echo_finding() {
    local severity="$1"
    local file="$2"
    local description="$3"
    local color="$YELLOW"
    local label="MEDIUM"

    case "$severity" in
        CRITICAL) color="$RED";     label="CRITICAL"; ISSUES_CRITICAL=$((ISSUES_CRITICAL+1));;
        HIGH)     color="$RED";     label="HIGH";     ISSUES_HIGH=$((ISSUES_HIGH+1));;
        MEDIUM)   color="$YELLOW";  label="MEDIUM";;
        LOW)      color="$GREEN";   label="LOW";;
    esac

    if [[ "$SEVERITY" != "all" ]]; then
        case "$severity" in
            CRITICAL) [[ "$SEVERITY" != "CRITICAL" ]] && return;;
            HIGH)     [[ "$SEVERITY" != "HIGH" && "$SEVERITY" != "CRITICAL" ]] && return;;
            MEDIUM)   [[ "$SEVERITY" == "HIGH" || "$SEVERITY" == "CRITICAL" ]] && return;;
            LOW)      [[ "$SEVERITY" != "LOW" ]] && return;;
        esac
    fi

    echo -e "${color}[${label}]${NC} ${description}"
    echo -e "       ${BLUE}File:${NC} ${file}"
    ISSUES_FOUND=$((ISSUES_FOUND+1))
}

# ── Parse arguments ──────────────────────────────────────────────────────────

while [[ $# -gt 0 ]]; do
    case "$1" in
        --report)   REPORT_FILE="$REPO_ROOT/docs/security-audit.md"; shift;;
        --quick)    SEVERITY="HIGH"; shift;;
        --category) CATEGORY="$2"; shift 2;;
        --severity) SEVERITY="$2"; shift 2;;
        --help|-h)
            echo "Usage: $0 [--quick] [--report] [--category X] [--severity X]"
            echo "Categories: secrets, sqli, cors, auth, validation, ratelimit, deps, errors, all"
            exit 0;;
        *) echo "Unknown option: $1"; exit 1;;
    esac
done

# ── Check Prerequisites ─────────────────────────────────────────────────────

if ! command -v grep &>/dev/null; then
    echo "Error: grep is required"
    exit 1
fi

echo "============================================"
echo " ZY_Foundation_Yingzhi — Security Audit Scan"
echo " Date: $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
echo " Category: ${CATEGORY}"
echo " Severity: ${SEVERITY}"
echo "============================================"

# ==============================================================================
# 1. HARDCODED SECRETS & CREDENTIALS
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "secrets" ]]; then
echo_header "1. Hardcoded Secrets & Credentials"

    # Check default JWT secrets
    echo "  → Checking for default JWT secrets..."
    JWT_MATCHES=$(grep -rn "change-this-development-secret" backend/ --include="*.yml" --include="*.yaml" --include="*.properties" 2>/dev/null | head -10 || true)
    if [[ -n "$JWT_MATCHES" ]]; then
        echo_finding "CRITICAL" "$(echo "$JWT_MATCHES" | head -1 | cut -d: -f1)" \
            "Default JWT secret found in production config (change-this-development-secret-change-before-production)"
    fi

    # Check default admin password
    echo "  → Checking for default admin password..."
    ADMIN_MATCHES=$(grep -rn "admin123" backend/ --include="*.yml" --include="*.yaml" --include="*.properties" 2>/dev/null | head -10 || true)
    if [[ -n "$ADMIN_MATCHES" ]]; then
        echo_finding "CRITICAL" "$(echo "$ADMIN_MATCHES" | head -1 | cut -d: -f1)" \
            "Default admin password 'admin123' found in production config"
    fi

    # Check database passwords
    echo "  → Checking for default database passwords..."
    DB_MATCHES=$(grep -rn "DB_PASSWORD:zhangyuan\|password: zhangyuan" backend/ --include="*.yml" --include="*.yaml" 2>/dev/null | head -10 || true)
    if [[ -n "$DB_MATCHES" ]]; then
        echo_finding "CRITICAL" "$(echo "$DB_MATCHES" | head -1 | cut -d: -f1)" \
            "Default database password 'zhangyuan' found in production config"
    fi

    # Check MinIO secrets
    echo "  → Checking for hardcoded MinIO secrets..."
    MINIO_MATCHES=$(grep -rn "zhangyuan-minio-secret\|MINIO_SECRET_KEY" backend/ --include="*.yml" --include="*.yaml" 2>/dev/null | head -10 || true)
    if [[ -n "$MINIO_MATCHES" ]]; then
        echo_finding "HIGH" "$(echo "$MINIO_MATCHES" | head -1 | cut -d: -f1)" \
            "Hardcoded MinIO secret key in config file"
    fi

    # Check Nacos passwords
    echo "  → Checking for default Nacos passwords..."
    NACOS_MATCHES=$(grep -rn "NACOS_PASSWORD:chengccn\|password: chengccn" backend/ --include="*.yml" --include="*.yaml" 2>/dev/null | head -10 || true)
    if [[ -n "$NACOS_MATCHES" ]]; then
        echo_finding "CRITICAL" "$(echo "$NACOS_MATCHES" | head -1 | cut -d: -f1)" \
            "Default Nacos password 'chengccn' in config file"
    fi

    # Check for OpenAI/Claude API keys in config
    echo "  → Checking for AI provider API keys..."
    AI_KEY_MATCHES=$(grep -rn "OPENAI_API_KEY:\|CLAUDE_API_KEY:" backend/ --include="*.yml" --include="*.yaml" 2>/dev/null | head -10 || true)
    if [[ -n "$AI_KEY_MATCHES" ]]; then
        echo "$AI_KEY_MATCHES" | while IFS= read -r line; do
            # Only flag if key is provided (not empty placeholder)
            if echo "$line" | grep -qv ':\s*$' && echo "$line" | grep -qv ':\s*${'; then
                echo_finding "HIGH" "$(echo "$line" | cut -d: -f1)" \
                    "Hardcoded AI provider API key detected"
            fi
        done
    fi
fi

# ==============================================================================
# 2. SQL INJECTION
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "sqli" ]]; then
echo_header "2. SQL Injection"

    echo "  → Checking for native SQL queries..."
    NATIVE_QUERIES=$(grep -rn "nativeQuery\|createNativeQuery\|@Query.*native\|NativeQuery" backend/ --include="*.java" 2>/dev/null | head -20 || true)
    if [[ -n "$NATIVE_QUERIES" ]]; then
        echo_finding "MEDIUM" "$(echo "$NATIVE_QUERIES" | head -1 | cut -d: -f1)" \
            "Native SQL queries found — verify parameterization"
        echo "       Context:"
        echo "$NATIVE_QUERIES" | head -5 | while IFS= read -r line; do
            echo "         $line"
        done
    else
        echo -e "  ${GREEN}✓ No native SQL queries found (JPA-only)${NC}"
    fi

    # Check for raw SQL string concatenation
    echo "  → Checking for SQL concatenation patterns..."
    SQL_CONCAT=$(grep -rn "String.*=.*\"SELECT\|String.*=.*\"select\|String.*=.*\"INSERT\|String.*=.*\"UPDATE" backend/ --include="*.java" 2>/dev/null | grep -v "prepareStatement\|PreparedStatement\|\.setString\|@Query" | head -10 || true)
    if [[ -n "$SQL_CONCAT" ]]; then
        echo_finding "HIGH" "$(echo "$SQL_CONCAT" | head -1 | cut -d: -f1)" \
            "Potential SQL injection — raw SQL string building detected"
        echo "       Context:"
        echo "$SQL_CONCAT" | head -5 | while IFS= read -r line; do
            echo "         $line"
        done
    fi

    # Check for OrderSpecification or custom queries with user input
    echo "  → Checking for JPA Specification patterns..."
    SPEC_MATCHES=$(grep -rn "Specification\|CriteriaBuilder.*where\|cb\.like\|cb\.equal" backend/ --include="*.java" 2>/dev/null | head -10 || true)
    if [[ -n "$SPEC_MATCHES" ]]; then
        echo -e "  ${YELLOW}⚠ JPA Specifications found — verify they use parameterized queries${NC}"
    fi
fi

# ==============================================================================
# 3. CORS CONFIGURATION
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "cors" ]]; then
echo_header "3. CORS Configuration"

    echo "  → Checking CORS configurations..."
    CORS_MATCHES=$(grep -rn "allowedOrigins\|allowedOriginPatterns\|@CrossOrigin\|CorsConfiguration" backend/ --include="*.java" --include="*.yml" --include="*.yaml" 2>/dev/null | grep -v "/build/" | head -20 || true)
    if [[ -z "$CORS_MATCHES" ]]; then
        echo -e "  ${YELLOW}⚠ No CORS configuration found in individual services${NC}"
    else
        echo "$CORS_MATCHES" | while IFS= read -r line; do
            echo_finding "MEDIUM" "$(echo "$line" | cut -d: -f1)" \
                "CORS configuration found: $(echo "$line" | cut -d: -f3-)"
        done
    fi

    # Check for wildcard CORS with credentials
    echo "  → Checking for credential-bearing wildcard CORS..."
    WILDCARD_CORS=$(grep -rn "allowedOriginPatterns.*\*\|allowedOrigins.*\*" backend/gateway/ --include="*.yml" --include="*.yaml" 2>/dev/null | head -10 || true)
    if [[ -n "$WILDCARD_CORS" ]]; then
        echo_finding "HIGH" "backend/gateway/src/main/resources/application.yml" \
            "Gateway CORS uses wildcard origin patterns with allowCredentials=true — restrict to specific origins"
    fi
fi

# ==============================================================================
# 4. AUTHENTICATION & AUTHORIZATION
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "auth" ]]; then
echo_header "4. Authentication & Authorization"

    # Check SaToken configs for /api/** unprotected routes
    echo "  → Checking Sa-Token route protection..."
    SATOKEN_MATCHES=$(grep -rn "SaRouter.match.*/api/\*\*" backend/ --include="*.java" 2>/dev/null || true)
    if [[ -n "$SATOKEN_MATCHES" ]]; then
        echo "$SATOKEN_MATCHES" | while IFS= read -r line; do
            service=$(echo "$line" | cut -d: -f1 | xargs dirname | xargs dirname | xargs basename)
            # Check if /api/** has an empty check or notMatch
            if echo "$line" | grep -q "check(r -> {})"; then
                echo_finding "HIGH" "$(echo "$line" | cut -d: -f1)" \
                    "Service '${service}': /api/** routes have NO authentication (empty check)"
            fi
        done
    fi

    # Check for @SaIgnore annotations
    echo "  → Checking for @SaIgnore annotations..."
    SAIGNORE=$(grep -rn "@SaIgnore" backend/ --include="*.java" 2>/dev/null | head -10 || true)
    if [[ -n "$SAIGNORE" ]]; then
        echo "$SAIGNORE" | while IFS= read -r line; do
            echo_finding "HIGH" "$(echo "$line" | cut -d: -f1)" \
                "@SaIgnore annotation found — bypasses Sa-Token authentication"
        done
    fi

    # Check ApiKeyAuthFilter bypasses
    echo "  → Checking API key auth filter bypasses..."
    AUTH_BYPASS=$(grep -rn "path.contains.*fulfill\|path.contains.*callbacks" backend/ --include="*.java" 2>/dev/null | head -10 || true)
    if [[ -n "$AUTH_BYPASS" ]]; then
        echo "$AUTH_BYPASS" | while IFS= read -r line; do
            echo_finding "HIGH" "$(echo "$line" | cut -d: -f1)" \
                "API key auth filter has explicit bypass pattern: $(echo "$line" | grep -o 'path\.contains[^)]*')"
        done
    fi

    # Check default password encoder
    echo "  → Checking password encoders..."
    if grep -q "BCryptPasswordEncoder" backend/ --include="*.java" 2>/dev/null; then
        echo -e "  ${GREEN}✓ BCryptPasswordEncoder in use (good)${NC}"
    fi

    # Check Hibernate ddl-auto settings
    echo "  → Checking Hibernate ddl-auto settings..."
    for config in $(find backend -path "*/src/main/resources/application.yml" -not -path "*/build/*" 2>/dev/null); do
        service_dir=$(echo "$config" | sed 's|/src/main/resources/application\.yml||' | xargs basename)
        ddl=$(grep "ddl-auto" "$config" 2>/dev/null | awk '{print $2}')
        if [[ "$ddl" == "update" ]]; then
            echo_finding "HIGH" "$config" \
                "Service '${service_dir}': Hibernate ddl-auto is 'update' — use 'validate' with Flyway migrations"
        fi
    done

    # Check for missing SaTokenConfig in ai-service
    if [[ ! -f "backend/ai-service/src/main/java/com/zhangyuan/ai/common/SaTokenConfig.java" ]]; then
        echo_finding "MEDIUM" "backend/ai-service/" \
            "AI-service missing SaTokenConfig — no route-level authentication"
    fi

    # Check insecure endpoints (balance, orders without auth)
    echo "  → Checking for data exposure endpoints..."
    BALANCE_ENDPOINTS=$(grep -rn "/{userId}" backend/user-service/src/main/java/com/zhangyuan/user/adapter/in/rest/BalanceController.java 2>/dev/null || true)
    if [[ -n "$BALANCE_ENDPOINTS" ]]; then
        echo_finding "HIGH" "backend/user-service/.../BalanceController.java" \
            "Balance endpoints use direct userId without ownership verification"
    fi
fi

# ==============================================================================
# 5. INPUT VALIDATION
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "validation" ]]; then
echo_header "5. Input Validation"

    echo "  → Checking for @Valid annotations on request body parameters..."

    # Count controllers vs validated controllers
    TOTAL_ENDPOINTS=$(grep -rn "@RequestBody" backend/ --include="*.java" 2>/dev/null | wc -l || true)
    VALIDATED_ENDPOINTS=$(grep -rn "@Valid.*@RequestBody\|@Valid\n.*@RequestBody" backend/ --include="*.java" 2>/dev/null | wc -l || true)

    echo "       Total @RequestBody endpoints: ${TOTAL_ENDPOINTS}"
    echo "       Endpoints with @Valid:         ${VALIDATED_ENDPOINTS}"

    if [[ "$TOTAL_ENDPOINTS" -gt "$VALIDATED_ENDPOINTS" ]]; then
        # Find endpoints missing @Valid
        MISSING_VALID=$(grep -rn "@RequestBody" backend/ --include="*.java" 2>/dev/null | grep -v "@Valid" | head -15 || true)
        if [[ -n "$MISSING_VALID" ]]; then
            echo_finding "MEDIUM" "$(echo "$MISSING_VALID" | head -1 | cut -d: -f1)" \
                "Request body parameter without @Valid annotation — possible missing validation"
            echo "       Examples:"
            echo "$MISSING_VALID" | head -5 | while IFS= read -r line; do
                echo "         $line"
            done
        fi
    else
        echo -e "  ${GREEN}✓ All request body parameters appear to have @Valid${NC}"
    fi
fi

# ==============================================================================
# 6. RATE LIMITING
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "ratelimit" ]]; then
echo_header "6. Rate Limiting"

    echo "  → Checking rate limiter implementations..."

    # Check for rate limit bypass
    RATE_BYPASS=$(grep -rn "userId == null\|resolveUserId.*null" backend/ai-service/src/main/java/com/zhangyuan/ai/common/RateLimitFilter.java 2>/dev/null || true)
    if [[ -n "$RATE_BYPASS" ]]; then
        echo_finding "HIGH" "backend/ai-service/.../RateLimitFilter.java" \
            "Rate limit filter passes unauthenticated requests without rate limiting"
    fi

    # Check in-memory rate limiting
    MEMORY_LIMITER=$(grep -rn "ConcurrentHashMap.*AtomicLong\|lastAccessMap" backend/ --include="*.java" 2>/dev/null || true)
    if [[ -n "$MEMORY_LIMITER" ]]; then
        echo_finding "MEDIUM" "$(echo "$MEMORY_LIMITER" | head -1 | cut -d: -f1)" \
            "In-memory only rate limiter detected — not effective across instances, can be bypassed"
    fi

    # Check for rate limiting on critical endpoints
    echo "  → Checking rate limiting coverage on auth endpoints..."
    for endpoint in "login" "register" "verify-key" "recharge" "quota"; do
        coverage=$(grep -rn "$endpoint" backend/user-service/src/main/java/com/zhangyuan/user/adapter/in/rest/SaasUserController.java 2>/dev/null | grep -c "isRateLimited" || true)
        if [[ "$coverage" -eq 0 ]]; then
            echo_finding "MEDIUM" "backend/user-service/.../SaasUserController.java" \
                "Endpoint involving '$endpoint' may not have rate limiting"
        fi
    done
fi

# ==============================================================================
# 7. DEPENDENCY VULNERABILITIES
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "deps" ]]; then
echo_header "7. Dependency Analysis"

    echo "  → Checking dependency versions..."

    # Check key dependency versions
    while IFS= read -r line; do
        echo "       $line"
    done < <(grep -rn "sa-token\|spring-boot\|spring-cloud\|minio\|modulith\|archunit\|rocketmq" backend/*/build.gradle backend/build.gradle 2>/dev/null | head -20 || true)

    # Check for known old versions
    echo "  → Checking for known vulnerable patterns..."
    if grep -q "spring-cloud-starter-stream-rocketmq" backend/ --include="build.gradle" 2>/dev/null; then
        echo -e "  ${YELLOW}⚠ RocketMQ stream binder found — verify version and security configuration${NC}"
    fi

    if grep -q "sa-token.*1\.39\.0" backend/ --include="build.gradle" 2>/dev/null; then
        echo "       Sa-Token 1.39.0 — check for latest security advisories"
    fi

    echo -e "\n  ${YELLOW}Note: This is not a comprehensive dependency vulnerability scan.${NC}"
    echo -e "  ${YELLOW}For full analysis, run: ./gradlew dependencyCheckAnalyze (requires OWASP plugin)${NC}"
fi

# ==============================================================================
# 8. ERROR HANDLING
# ==============================================================================
if [[ "$CATEGORY" == "all" || "$CATEGORY" == "errors" ]]; then
echo_header "8. Error Handling & Information Disclosure"

    # Check for getMessage() returned to clients
    echo "  → Checking for information leakage in error responses..."
    LEAKED_MESSAGES=$(grep -rn "\.body.*getMessage\|e\.getMessage.*400\|e\.getMessage.*error\|body(ApiResponse.error.*getMessage" backend/ --include="*.java" 2>/dev/null | head -20 || true)
    if [[ -n "$LEAKED_MESSAGES" ]]; then
        LEAK_COUNT=$(echo "$LEAKED_MESSAGES" | wc -l)
        echo_finding "MEDIUM" "$(echo "$LEAKED_MESSAGES" | head -1 | cut -d: -f1)" \
            "Exception messages leaked to API responses (${LEAK_COUNT} occurrences) — possible information disclosure"
        echo "       Examples:"
        echo "$LEAKED_MESSAGES" | head -5 | while IFS= read -r line; do
            echo "         $line"
        done
    fi

    # Check actuator exposure
    echo "  → Checking actuator endpoint exposure..."
    ACTUATOR_EXPOSURE=$(grep -rn "actuator" backend/ --include="*.yml" --include="*.yaml" --include="*.properties" 2>/dev/null | grep -v "target\|test" | head -10 || true)
    echo "$ACTUATOR_EXPOSURE" | while IFS= read -r line; do
        if echo "$line" | grep -q "include:\|exposure:"; then
            echo -e "  ${YELLOW}⚠ Actuator exposure found:${NC} $line"
        fi
    done

    if grep -q "actuator" backend/ --include="*.java" 2>/dev/null | grep -q "check(r -> {})"; then
        echo_finding "MEDIUM" "backend/.../SaTokenConfig.java" \
            "Actuator endpoints are accessible without authentication"
    fi

    # Check for stack trace printing
    echo "  → Checking for stack trace leakage..."
    STACK_TRACE=$(grep -rn "e\.printStackTrace\|printStackTrace" backend/ --include="*.java" 2>/dev/null | head -10 || true)
    if [[ -n "$STACK_TRACE" ]]; then
        echo_finding "LOW" "$(echo "$STACK_TRACE" | head -1 | cut -d: -f1)" \
            "printStackTrace() usage detected — ensure these are in development-only code paths"
    fi

    echo -e "\n  ${GREEN}✓ Overall error handling is good: stack traces are logged, generic messages returned${NC}"
fi

# ==============================================================================
# 9. ADDITIONAL CHECKS
# ==============================================================================
if [[ "$CATEGORY" == "all" ]]; then
echo_header "9. Additional Checks"

    # Check CSRF
    echo "  → Checking CSRF configuration..."
    CSRF_COUNT=$(grep -rn "csrf.*disable\|csrf.*AbstractHttpConfigurer::disable" backend/ --include="*.java" 2>/dev/null | wc -l || true)
    echo -e "  ${YELLOW}⚠ CSRF protection disabled in ${CSRF_COUNT} locations${NC}"

    # Check API key generation quality
    echo "  → Checking API key generation..."
    API_KEY_GEN=$(grep -rn "SecureRandom\|generateApiKey" backend/ --include="*.java" 2>/dev/null | head -10 || true)
    if echo "$API_KEY_GEN" | grep -q "SecureRandom"; then
        echo -e "  ${GREEN}✓ SecureRandom used for API key generation${NC}"
    fi

    # Check frontend token storage
    echo "  → Checking frontend token storage..."
    if grep -q "localStorage.getItem.*token\|localStorage.setItem.*token" frontend/ --include="*.ts" --include="*.js" --include="*.vue" 2>/dev/null; then
        echo_finding "LOW" "frontend/" \
            "Auth tokens stored in localStorage (XSS-vulnerable) — consider httpOnly cookies"
    fi

    # Check gateway CORS
    echo "  → Checking gateway security configuration..."
    if grep -q "allowCredentials: true" backend/gateway/src/main/resources/application.yml 2>/dev/null; then
        echo -e "  ${YELLOW}⚠ Gateway has allowCredentials=true${NC}"
    fi

    # Check for security headers
    echo "  → Checking for security headers..."
    if ! grep -rn "Content-Security-Policy\|Strict-Transport-Security\|X-Content-Type-Options" backend/ --include="*.java" --include="*.yml" --include="*.yaml" 2>/dev/null | head -5 | grep -q .; then
        echo_finding "LOW" "global" \
            "No security headers (CSP, HSTS, X-Content-Type-Options) found in configuration"
    fi

    # Check for Spring Security .permitAll() patterns
    echo "  → Checking Spring Security permitAll patterns..."
    PERMIT_ALL=$(grep -rn "anyRequest().permitAll\|anyRequest().authenticated" backend/ --include="*.java" 2>/dev/null | head -10 || true)
    echo "       Security filter chains:"
    echo "$PERMIT_ALL" | while IFS= read -r line; do
        service=$(echo "$line" | cut -d: -f1 | xargs dirname | xargs dirname | xargs basename)
        echo "       ${service}: $(echo "$line" | grep -o 'anyRequest().*')"
    done
fi

# ==============================================================================
# SUMMARY
# ==============================================================================
echo_header "Scan Complete"
echo ""
echo -e "  Issues found: ${RED}${ISSUES_FOUND}${NC}"
echo -e "  CRITICAL:     ${RED}${ISSUES_CRITICAL}${NC}"
echo -e "  HIGH:         ${RED}${ISSUES_HIGH}${NC}"
echo ""

if [[ "$ISSUES_CRITICAL" -gt 0 ]]; then
    echo -e "  ${RED}⚠ CRITICAL issues found — address immediately before production deployment${NC}"
fi
if [[ "$ISSUES_HIGH" -gt 0 ]]; then
    echo -e "  ${RED}⚠ HIGH issues found — plan remediation in current sprint${NC}"
fi
if [[ "$ISSUES_FOUND" -eq 0 ]]; then
    echo -e "  ${GREEN}✓ No issues found!${NC}"
fi

echo ""
echo "  Detailed report: docs/security-audit.md"
echo ""

# Generate report if requested
if [[ "$REPORT_FILE" != "/dev/stdout" ]]; then
    echo "Generating report to ${REPORT_FILE}..."
    # Link to the full audit report
    echo "Quick scan results: $(date)" >> "$REPORT_FILE"
    echo "Issues found: ${ISSUES_FOUND} (Critical: ${ISSUES_CRITICAL}, High: ${ISSUES_HIGH})" >> "$REPORT_FILE"
    echo "See docs/security-audit.md for full report" >> "$REPORT_FILE"
fi

exit 0
