param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$Username = "admin",
  [string]$Password = "admin123"
)

$ErrorActionPreference = "Stop"

# ---------- Helper ----------
function Api([string]$Method, [string]$Path, $Body, $Headers) {
  $uri = "$BaseUrl$Path"
  $json = if ($Body) { $Body | ConvertTo-Json -Depth 10 } else { $null }
  try {
    $r = Invoke-WebRequest -Uri $uri -Method $Method -Body $json `
      -ContentType "application/json" -Headers $Headers -UseBasicParsing
    if ($r.Content) { return $r.Content | ConvertFrom-Json }
    return $null
  } catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Warning "$Method $Path → $status"
    return $null
  }
}

function GetToken {
  $r = Api "POST" "/admin/auth/login" @{username=$Username; password=$Password}
  if (-not $r -or $r.code -ne 0) { throw "Login failed" }
  Write-Host "  ✓ Login as $Username"
  return @{Authorization = "Bearer $($r.data.accessToken)"}
}

# ---------- Main ----------
Write-Host "========================================"
Write-Host "  Zhangyuan Test Data Seeder"
Write-Host "========================================"
Write-Host ""

$h = GetToken

# ---- CMS: draft + publish existing pages ----
Write-Host "`n[1/5] CMS Pages – draft & publish"
$pages = (Api "GET" "/admin/cms/pages" -Headers $h).data
if (-not $pages) { Write-Host "  - No pages found, skip" }
else {
  foreach ($p in $pages) {
    $locale = $p.defaultLocale
    $draftBody = @{title = "Page $($p.slug)"; seoTitle = ""; seoDescription = ""; seoKeywords = ""; content = @{}}
    $r1 = Api "PUT" "/admin/cms/pages/$($p.id)/translations/$locale/draft" $draftBody $h
    if ($r1) { Write-Host "  ✓ Draft saved: $($p.slug)" }
    $r2 = Api "POST" "/admin/cms/pages/$($p.id)/translations/$locale/publish" @{} $h
    if ($r2) { Write-Host "  ✓ Published: $($p.slug)" }
    Start-Sleep -Milliseconds 200
  }
}

# ---- Product: plan group + plan + price + feature ----
Write-Host "`n[2/5] Product – plan groups & plans"
$group = (Api "POST" "/admin/product/plan-groups" @{code="basic"; name="Basic Plan"; description="Entry-level tier"; sortOrder=1} $h).data
if ($group) { Write-Host "  ✓ Plan group: $($group.code) (id=$($group.id))" }

$plan = (Api "POST" "/admin/product/plans" @{groupId=$group.id; code="basic-monthly"; name="Monthly Basic"; description="Monthly subscription"; sortOrder=1} $h).data
if ($plan) { Write-Host "  ✓ Plan: $($plan.code) (id=$($plan.id))" }

$r3 = Api "POST" "/admin/product/prices" @{planId=$plan.id; currency="CNY"; billingCycle="monthly"; amount=29.99; originalAmount=49.99} $h
if ($r3) { Write-Host "  ✓ Price: monthly 29.99 CNY" }

$r4 = Api "POST" "/admin/product/features" @{planId=$plan.id; featureName="Users"; featureValue="10 users"; included=$true; sortOrder=1} $h
if ($r4) { Write-Host "  ✓ Feature: Users = 10" }

# ---- Premium plan group ----
$group2 = (Api "POST" "/admin/product/plan-groups" @{code="premium"; name="Premium Plan"; description="Advanced tier"; sortOrder=2} $h).data
if ($group2) { Write-Host "  ✓ Plan group: $($group2.code) (id=$($group2.id))" }

$plan2 = (Api "POST" "/admin/product/plans" @{groupId=$group2.id; code="premium-yearly"; name="Yearly Premium"; description="Yearly subscription with discount"; sortOrder=1} $h).data
if ($plan2) { Write-Host "  ✓ Plan: $($plan2.code) (id=$($plan2.id))" }

$r5 = Api "POST" "/admin/product/prices" @{planId=$plan2.id; currency="CNY"; billingCycle="yearly"; amount=199.99; originalAmount=399.99} $h
if ($r5) { Write-Host "  ✓ Price: yearly 199.99 CNY" }

$r6 = Api "POST" "/admin/product/features" @{planId=$plan2.id; featureName="Storage"; featureValue="100 GB"; included=$true; sortOrder=1} $h
if ($r6) { Write-Host "  ✓ Feature: Storage = 100 GB" }

# ---- Order ----
Write-Host "`n[3/5] Orders"
$order = (Api "POST" "/api/orders" @{planCode="basic-monthly"; billingCycle="monthly"; currency="CNY"} $h).data
if ($order) { Write-Host "  ✓ Order: $($order.orderNo)" }

# ---- Payment ----
Write-Host "`n[4/5] Payments"
$payment = (Api "POST" "/api/payments/checkout" @{orderNo=$order.orderNo; channel="mock"} $h).data
if ($payment) {
  Write-Host "  ✓ Payment: $($payment.paymentNo)"
  Start-Sleep -Milliseconds 200
  $r7 = Api "POST" "/api/payments/mock/$($payment.paymentNo)/success" $null $h
  if ($r7) { Write-Host "  ✓ Payment marked success" }
}

# ---- System Settings ----
Write-Host "`n[5/5] System Settings"
$settings = @(
  @{key="site_name"; value="Zhangyuan CMS"}
  @{key="site_description"; value="A modern content management platform"}
  @{key="contact_email"; value="admin@zhangyuan.dev"}
)
foreach ($s in $settings) {
  $r8 = Api "PUT" "/admin/system/settings/$($s.key)" $s $h
  if ($r8) { Write-Host "  ✓ Setting: $($s.key) = $($s.value)" }
}

Write-Host ""
Write-Host "========================================"
Write-Host "  Seed complete!"
Write-Host "========================================"
