param([string]$Phase = "all")
$ErrorActionPreference = "Continue"

$gw = "http://localhost:8080"; $api = "http://localhost:8088"; $a = "http://localhost:8082"
$s = "http://localhost:8081"; $u = "http://localhost:8085"; $o = "http://localhost:8083"; $p = "http://localhost:8084"
$suffix = "$(Get-Random -Min 10000 -Max 99999)"

if (-not (Test-Path "$env:TEMP\e2e.ps1")) {
    Write-Host "=== Phase 0: Get Credentials ===" -ForegroundColor Cyan
    $login = Invoke-RestMethod -Uri "$a/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 60
    $token = "Bearer $($login.data.accessToken)"
    $email = "t$(Get-Random -Min 10000 -Max 99999)@e2e.com"
    $reg = Invoke-RestMethod -Uri "$gw/api/auth/register" -Method Post -ContentType "application/json" -Body "{`"email`":`"$email`",`"password`":`"Test123456`",`"nickname`":`"E2E`"}" -TimeoutSec 15
    $st = "Bearer $($reg.data.token)"
    $vars = @"
`$gw="$gw"
`$api="$api"
`$a="$a"
`$s="$s"
`$u="$u"
`$o="$o"
`$p="$p"
`$token="$token"
`$st="$st"
"@
    $vars | Out-File "$env:TEMP\e2e.ps1" -Encoding utf8
    Write-Host "[OK] Admin Token + SaaS registered: $email" -ForegroundColor Green
}
. "$env:TEMP\e2e.ps1"
$h = @{Authorization=$token}
$pass = 0; $fail = 0

function Test-Ok($name, $result) {
    if ($result -and $result.code -eq 0) { $script:pass++; Write-Host "  [PASS] $name" -ForegroundColor Green }
    else { $script:fail++; $msg = if ($result) { $result.message } else { "no response" }; Write-Host "  [FAIL] $name ($msg)" -ForegroundColor Red }
}

function SafeApi($uri, $method="GET", $body=$null, $headers=$null, $timeout=15) {
    $params = @{Uri=$uri; Method=$method; ContentType="application/json"; TimeoutSec=$timeout}
    if ($body) { $params.Body = $body }
    if ($headers) { $params.Headers = $headers }
    try { return Invoke-RestMethod @params -ErrorAction Stop }
    catch { return $_.Exception.Response }
}

# ========== Phase 1: Reads ==========
if ($Phase -eq "all" -or $Phase -eq "1a") {
Write-Host "=== Phase 1a: Public API ===" -ForegroundColor Cyan
Test-Ok "CMS Render" (Invoke-RestMethod -Uri "$api/api/cms/pages/render?path=/&locale=zh-CN" -TimeoutSec 10)
Test-Ok "Product plan-groups" (Invoke-RestMethod -Uri "$api/api/products/plan-groups/basic" -TimeoutSec 10)
Test-Ok "Product plans" (Invoke-RestMethod -Uri "$api/api/products/plans" -TimeoutSec 10)
Test-Ok "DDD product" (Invoke-RestMethod -Uri "$api/api/ddd/product/plan-groups" -TimeoutSec 10)
Test-Ok "DDD asset" (Invoke-RestMethod -Uri "$api/api/ddd/assets" -TimeoutSec 10)
Test-Ok "DDD order" (Invoke-RestMethod -Uri "$api/api/ddd/orders" -TimeoutSec 10)
Test-Ok "DDD payment" (Invoke-RestMethod -Uri "$api/api/ddd/payments" -TimeoutSec 10)
Test-Ok "DDD cms" (Invoke-RestMethod -Uri "$api/api/ddd/cms/pages" -TimeoutSec 10)
Test-Ok "DDD auth users" (Invoke-RestMethod -Uri "$a/api/ddd/auth/users" -TimeoutSec 10)
Test-Ok "DDD auth roles" (Invoke-RestMethod -Uri "$a/api/ddd/auth/roles" -TimeoutSec 10)
Test-Ok "Actuator health" (Invoke-RestMethod -Uri "$api/actuator/health" -TimeoutSec 5)
Test-Ok "Actuator info" (Invoke-RestMethod -Uri "$api/actuator/info" -TimeoutSec 5)
}

if ($Phase -eq "all" -or $Phase -eq "1b") {
Write-Host "=== Phase 1b: Admin Reads ===" -ForegroundColor Cyan
Test-Ok "CMS pages" (Invoke-RestMethod -Uri "$api/admin/cms/pages" -Headers $h -TimeoutSec 10)
Test-Ok "Product plan-groups" (Invoke-RestMethod -Uri "$api/admin/product/plan-groups" -Headers $h -TimeoutSec 10)
Test-Ok "Product plans" (Invoke-RestMethod -Uri "$api/admin/product/plans" -Headers $h -TimeoutSec 10)
Test-Ok "Product prices" (Invoke-RestMethod -Uri "$api/admin/product/prices" -Headers $h -TimeoutSec 10)
Test-Ok "Asset files" (Invoke-RestMethod -Uri "$api/admin/assets/files" -Headers $h -TimeoutSec 10)
Test-Ok "Admin orders" (Invoke-RestMethod -Uri "$api/admin/orders" -Headers $h -TimeoutSec 10)
Test-Ok "Admin payments" (Invoke-RestMethod -Uri "$api/admin/payments" -Headers $h -TimeoutSec 10)
}

if ($Phase -eq "all" -or $Phase -eq "1c") {
Write-Host "=== Phase 1c: Auth Reads ===" -ForegroundColor Cyan
Test-Ok "Users list" (Invoke-RestMethod -Uri "$a/admin/system/users?page=1&pageSize=20" -Headers $h -TimeoutSec 10)
Test-Ok "Roles list" (Invoke-RestMethod -Uri "$a/admin/system/roles?page=1&pageSize=20" -Headers $h -TimeoutSec 10)
Test-Ok "Permissions list" (Invoke-RestMethod -Uri "$a/admin/system/permissions?page=1&pageSize=20" -Headers $h -TimeoutSec 10)
Test-Ok "Permission modules" (Invoke-RestMethod -Uri "$a/admin/system/permissions/modules" -Headers $h -TimeoutSec 10)
Test-Ok "Menus list" (Invoke-RestMethod -Uri "$a/admin/system/menus" -Headers $h -TimeoutSec 10)
Test-Ok "Current user" (Invoke-RestMethod -Uri "$a/admin/auth/me" -Headers $h -TimeoutSec 10)
Test-Ok "User menus" (Invoke-RestMethod -Uri "$a/admin/auth/menus" -Headers $h -TimeoutSec 10)
}

if ($Phase -eq "all" -or $Phase -eq "1d") {
Write-Host "=== Phase 1d: Gateway + System ===" -ForegroundColor Cyan
Test-Ok "Gateway to API" (Invoke-RestMethod -Uri "$gw/api/cms/pages/render?path=/&locale=zh-CN" -TimeoutSec 10)
$r = Invoke-WebRequest -Uri "$gw/actuator/health" -UseBasicParsing -TimeoutSec 5
if ($r.StatusCode -eq 200) { $pass++; Write-Host "  [PASS] Gateway to Actuator" -ForegroundColor Green } else { $fail++; Write-Host "  [FAIL] Gateway to Actuator" -ForegroundColor Red }
}

# ========== Phase 2: Writes ==========
if ($Phase -eq "all" -or $Phase -eq "2a") {
Write-Host "=== Phase 2a: Product Chain ===" -ForegroundColor Cyan
$r = SafeApi "$api/admin/product/plan-groups" POST "{`"code`":`"e2e-pg-$suffix`",`"name`":`"E2E Group $suffix`",`"description`":`"E2E`",`"sortOrder`":1}" $h 15
if ($r.code -eq 0) { $pass++; $gid = $r.data.id; Write-Host "  [PASS] Create plan-group id=$gid" -ForegroundColor Green
    $r = SafeApi "$api/admin/product/plans" POST "{`"groupId`":$gid,`"code`":`"e2e-plan-$suffix`",`"name`":`"E2E Plan $suffix`",`"description`":`"E2E`",`"sortOrder`":1}" $h 15
    if ($r.code -eq 0) { $pass++; $planId = $r.data.id; Write-Host "  [PASS] Create plan id=$planId" -ForegroundColor Green
        Test-Ok "Create price" (SafeApi "$api/admin/product/prices" POST "{`"planId`":$planId,`"currency`":`"CNY`",`"billingCycle`":`"monthly`",`"amount`":29.99,`"originalAmount`":49.99}" $h 15)
        Test-Ok "Create feature" (SafeApi "$api/admin/product/features" POST "{`"planId`":$planId,`"featureName`":`"Users`",`"featureValue`":`"10`",`"included`":`$true,`"sortOrder`":1}" $h 15)
    } else { $fail++; Write-Host "  [FAIL] Create plan" -ForegroundColor Red }
} else { $fail++; Write-Host "  [FAIL] Create plan-group" -ForegroundColor Red }
}

if ($Phase -eq "all" -or $Phase -eq "2b") {
Write-Host "=== Phase 2b: CMS Chain ===" -ForegroundColor Cyan
$r = SafeApi "$api/admin/cms/pages" POST "{`"slug`":`"e2e-page-$suffix`",`"defaultLocale`":`"zh-CN`",`"title`":`"E2E Page $suffix`"}" $h 15
if ($r.code -eq 0) { $pageId = $r.data.id; $pass++; Write-Host "  [PASS] Create page id=$pageId" -ForegroundColor Green
    Test-Ok "Save draft" (SafeApi "$api/admin/cms/pages/$pageId/translations/zh-CN/draft" PUT '{"title":"E2E Draft","seoTitle":"","seoDescription":"","seoKeywords":"","content":{"blocks":[{"type":"text","data":{"content":"Hello"}}]}}' $h 15)
    Test-Ok "Read draft" (SafeApi "$api/admin/cms/pages/$pageId/translations/zh-CN/draft" GET $null $h 10)
    Test-Ok "Publish" (SafeApi "$api/admin/cms/pages/$pageId/translations/zh-CN/publish" POST '{}' $h 15)
    Test-Ok "Versions" (SafeApi "$api/admin/cms/pages/$pageId/translations/zh-CN/versions" GET $null $h 10)
    Test-Ok "Render" (SafeApi "$api/api/cms/pages/render?path=/e2e-page-$suffix&locale=zh-CN" GET $null $null 10)
} else { $fail++; Write-Host "  [FAIL] Create page" -ForegroundColor Red }
}

if ($Phase -eq "all" -or $Phase -eq "2c") {
Write-Host "=== Phase 2c: Auth Chain + Asset ===" -ForegroundColor Cyan
# Permission
$r = SafeApi "$a/admin/system/permissions" POST "{`"code`":`"perm:test-$suffix`",`"name`":`"E2E $suffix`",`"module`":`"system`",`"action`":`"test`"}" $h 15
if ($r.code -eq 0) { $permid = $r.data.id; $pass++; Write-Host "  [PASS] Create permission id=$permid" -ForegroundColor Green
    # Role with permission
    $r = SafeApi "$a/admin/system/roles" POST "{`"code`":`"e2e-role-$suffix`",`"name`":`"E2E Role $suffix`",`"description`":`"E2E`"}" $h 15
    if ($r.code -eq 0) { $rid = $r.data.id; $pass++; Write-Host "  [PASS] Create role id=$rid" -ForegroundColor Green
        Test-Ok "Set role permissions" (SafeApi "$a/admin/system/roles/$rid/permissions" PUT "{`"permissionIds`":[$permid]}" $h 15)
    } else { $fail++; Write-Host "  [FAIL] Create role" -ForegroundColor Red }
} else { $fail++; Write-Host "  [FAIL] Create permission" -ForegroundColor Red }
# Menu
$r = SafeApi "$a/admin/system/menus" POST "{`"parentId`":0,`"name`":`"E2E Menu $suffix`",`"icon`":`"settings`",`"path`":`"/e2e-$suffix`",`"sortOrder`":999,`"menuType`":`"MENU`"}" $h 15
if ($r.code -eq 0) { $pass++; Write-Host "  [PASS] Create menu id=$($r.data.id)" -ForegroundColor Green } else { $fail++; Write-Host "  [FAIL] Create menu" -ForegroundColor Red }
# DDD
Test-Ok "DDD create user" (SafeApi "$a/api/ddd/auth/users" POST "{`"username`":`"e2e-ddd-$suffix`",`"passwordHash`":`"Test123`",`"nickname`":`"E2E`"}" $h 15)
Test-Ok "DDD create role" (SafeApi "$a/api/ddd/auth/roles" POST "{`"code`":`"e2e-ddd-role-$suffix`",`"name`":`"DDD Role $suffix`"}" $h 15)
# Asset upload
$tf = "$env:TEMP\e2e-asset.txt"; "e2e" | Out-File $tf -Encoding ascii
try { $r = Invoke-RestMethod -Uri "$api/admin/assets/files" -Method Post -Headers @{Authorization=$token} -Form @{file=(Get-Item $tf)} -TimeoutSec 30
    if ($r.code -eq 0) { $pass++; Write-Host "  [PASS] Upload file id=$($r.data.id)" -ForegroundColor Green } else { $fail++; Write-Host "  [FAIL] Upload file" -ForegroundColor Red }
} catch { $fail++; Write-Host "  [FAIL] Upload file: $_" -ForegroundColor Red }
Remove-Item $tf -Force
}

# ========== Phase 3: Orders + Payment ==========
if ($Phase -eq "all" -or $Phase -eq "3") {
Write-Host "=== Phase 3: Order + Payment ===" -ForegroundColor Cyan
# Create a test plan group and plan for ordering
$r = SafeApi "$api/admin/product/plan-groups" POST "{`"code`":`"ord-pg-$suffix`",`"name`":`"Order PG`",`"description`":`"Order test`",`"sortOrder`":1}" $h 15
if ($r.code -eq 0) { $ogid = $r.data.id; $pass++; Write-Host "  [PASS] Order: create plan-group" -ForegroundColor Green
    $r2 = SafeApi "$api/admin/product/plans" POST "{`"groupId`":$ogid,`"code`":`"ord-plan-$suffix`",`"name`":`"Order Plan`",`"description`":`"Order test`",`"sortOrder`":1}" $h 15
    if ($r2.code -eq 0) { $opid = $r2.data.id; $pass++; Write-Host "  [PASS] Order: create plan" -ForegroundColor Green
        # Create a price for the plan
        $rp = SafeApi "$api/admin/product/prices" POST "{`"planId`":$opid,`"currency`":`"CNY`",`"billingCycle`":`"monthly`",`"amount`":99.00,`"originalAmount`":199.00}" $h 15
        if ($rp.code -eq 0) { $opriceid = $rp.data.id; $pass++; Write-Host "  [PASS] Order: create price" -ForegroundColor Green
            Test-Ok "DDD create order" (SafeApi "$api/api/ddd/orders" POST "{`"planId`":$opid,`"priceId`":$opriceid,`"amount`":99.00,`"currency`":`"CNY`"}" $h 15)
        } else { $fail++; Write-Host "  [FAIL] Order: create price" -ForegroundColor Red }
    } else { $fail++; Write-Host "  [FAIL] Order: create plan" -ForegroundColor Red }
} else { $fail++; Write-Host "  [FAIL] Order: create plan-group" -ForegroundColor Red }
# Payment checkout via API DDD
Test-Ok "DDD payment list" (SafeApi "$api/api/ddd/payments" GET $null $null 10)
# Usage via API DDD style
Test-Ok "Usage via order service" (SafeApi "$o/api/usage/1?page=1&pageSize=20" GET $null $null 10)
}

# ========== Phase 4: Security ==========
if ($Phase -eq "all" -or $Phase -eq "4") {
Write-Host "=== Phase 4: Security + Gateway ===" -ForegroundColor Cyan
try { Invoke-WebRequest -Uri "$api/admin/cms/pages" -Method Get -TimeoutSec 5 -ErrorAction Stop; $fail++ } catch { if ($_.Exception.Response.StatusCode -eq 401) { $pass++; Write-Host "  [PASS] No token -> 401" -ForegroundColor Green } else { $fail++; Write-Host "  [FAIL] No token -> $($_.Exception.Response.StatusCode)" -ForegroundColor Red } }
try { Invoke-WebRequest -Uri "$api/admin/cms/pages" -Method Get -Headers @{Authorization="Bearer invalid"} -TimeoutSec 5 -ErrorAction Stop; $fail++ } catch { if ($_.Exception.Response.StatusCode -eq 401) { $pass++; Write-Host "  [PASS] Invalid token -> 401" -ForegroundColor Green } else { $fail++; Write-Host "  [FAIL] Invalid token -> $($_.Exception.Response.StatusCode)" -ForegroundColor Red } }
Test-Ok "Public API no auth" (Invoke-RestMethod -Uri "$api/api/products/plan-groups/basic" -TimeoutSec 10)
$r = Invoke-RestMethod -Uri "$gw/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 30
if ($r.code -eq 0) { $pass++; Write-Host "  [PASS] Gateway login route" -ForegroundColor Green } else { $fail++; Write-Host "  [FAIL] Gateway login" -ForegroundColor Red }
}

Write-Host "`n=== Phase $Phase Results: $pass passed, $fail failed ===" -ForegroundColor $(if ($fail -eq 0) { 'Green' } else { if ($pass -gt $fail) { 'Yellow' } else { 'Red' } })
exit $fail
