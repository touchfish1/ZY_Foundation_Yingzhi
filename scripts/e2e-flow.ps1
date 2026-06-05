param([string]$Flow = "all")
$ErrorActionPreference = "Continue"

$gw = "http://localhost:8080"; $api = "http://localhost:8088"; $a = "http://localhost:8082"
$s = "http://localhost:8081"; $u = "http://localhost:8085"; $o = "http://localhost:8083"; $p = "http://localhost:8084"

$global:pass = 0; $global:fail = 0
function Ok($n) { $global:pass++; Write-Host "  [FLOW] $n" -ForegroundColor Green }
function Fail($n, $e) { $global:fail++; Write-Host "  [FAIL] $n`n         $e" -ForegroundColor Red }

# ============ FLOW A: SaaS 用户完整生命周期 ============
if ($Flow -eq "all" -or $Flow -eq "A") {
Write-Host "`n==========================================" -ForegroundColor Magenta
Write-Host " FLOW A: SaaS 用户完整生命周期" -ForegroundColor Magenta
Write-Host "   注册 → 登录 → 查余额 → 充值 → 查交易 → 查审计日志" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

try {
    $email = "flowA$(Get-Random -Min 10000 -Max 99999)@test.com"
    $reg = Invoke-RestMethod -Uri "$gw/api/auth/register" -Method Post -ContentType "application/json" -Body "{`"email`":`"$email`",`"password`":`"Test123456`",`"nickname`":`"FlowA`"}" -TimeoutSec 15
    if ($reg.code -ne 0) { throw "Register failed: $($reg.message)" }
    $uid = $reg.data.user.id; $st = "Bearer $($reg.data.token)"
    Ok "A1 注册成功 email=$email uid=$uid"

    $log2 = Invoke-RestMethod -Uri "$gw/api/auth/login" -Method Post -ContentType "application/json" -Body "{`"email`":`"$email`",`"password`":`"Test123456`"}" -TimeoutSec 15
    if ($log2.code -ne 0) { throw "Login failed" }
    $st = "Bearer $($log2.data.token)"
    Ok "A2 登录成功"

    $prof = Invoke-RestMethod -Uri "$u/api/auth/profile" -Headers @{Authorization=$st} -TimeoutSec 10
    if ($prof.code -ne 0) { throw "Profile failed: $($prof.message)" }
    Ok "A3 获取用户信息 email=$($prof.data.email)"

    $bal = Invoke-RestMethod -Uri "$u/api/balance/$uid" -Headers @{Authorization=$st} -TimeoutSec 10
    if ($bal.code -ne 0) { throw "Balance failed: $($bal.message)" }
    $bal0 = $bal.data.balance
    Ok "A4 查询余额 = $bal0"

    $rc = Invoke-RestMethod -Uri "$u/api/balance/$uid/recharge?amount=500&description=FlowATest" -Method Post -Headers @{Authorization=$st} -TimeoutSec 10
    if ($rc.code -ne 0) { throw "Recharge failed: $($rc.message)" }
    Ok "A5 充值 500 → 余额 = $($rc.data.balance)"

    $txn = Invoke-RestMethod -Uri "$u/api/balance/$uid/transactions" -Headers @{Authorization=$st} -TimeoutSec 10
    if ($txn.code -ne 0) { throw "Transactions failed" }
    Ok "A6 交易记录 = $($txn.data.Count) 笔"

    $log = Invoke-RestMethod -Uri "$u/api/logs?page=1&pageSize=10" -Headers @{Authorization=$st} -TimeoutSec 10
    if ($log.code -ne 0) { throw "Audit log failed" }
    Ok "A7 审计日志 = $($log.data.items.Count) 条"

} catch { Fail "Flow A", $_ }
}

# ============ FLOW B: 管理员内容发布流程 ============
if ($Flow -eq "all" -or $Flow -eq "B") {
Write-Host "`n==========================================" -ForegroundColor Magenta
Write-Host " FLOW B: 管理员内容发布流程" -ForegroundColor Magenta
Write-Host "   登录 → 创建页面 → 编辑草稿 → 发布 → 公开渲染 → 再次编辑 → 版本回滚" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

try {
    $log = Invoke-RestMethod -Uri "$a/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 30
    if ($log.code -ne 0) { throw "Login failed" }
    $tk = "Bearer $($log.data.accessToken)"
    Ok "B1 管理员登录"

    $page = Invoke-RestMethod -Uri "$api/admin/cms/pages" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"slug":"flow-b-page","defaultLocale":"zh-CN","title":"Flow B Page"}' -TimeoutSec 15
    if ($page.code -ne 0) { throw "Create page: $($page.message)" }
    $pid = $page.data.id
    Ok "B2 创建页面 id=$pid"

    $draft = Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid/translations/zh-CN/draft" -Method Put -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"title":"v1标题","seoTitle":"SEO标题","seoDescription":"SEO描述","seoKeywords":"kw1,kw2","content":{"blocks":[{"type":"hero","data":{"title":"Hello Flow B"}}]}}' -TimeoutSec 15
    if ($draft.code -ne 0) { throw "Draft: $($draft.message)" }
    Ok "B3 保存草稿 v1"

    $pub = Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid/translations/zh-CN/publish" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{}' -TimeoutSec 15
    if ($pub.code -ne 0) { throw "Publish: $($pub.message)" }
    Ok "B4 发布 v1"

    $ver = Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid/translations/zh-CN/versions" -Headers @{Authorization=$tk} -TimeoutSec 10
    if ($ver.code -ne 0) { throw "Versions: $($ver.message)" }
    $v1id = $ver.data[0].id; $v1no = $ver.data[0].versionNo
    Ok "B5 版本列表 = $($ver.data.Count) 个版本 (id=$v1id, no=$v1no)"

    $render = Invoke-RestMethod -Uri "$api/api/cms/pages/render?path=/flow-b-page&locale=zh-CN" -TimeoutSec 10
    if ($render.code -ne 0) { throw "Render: $($render.message)" }
    Ok "B6 公开渲染 → title=$($render.data.title)"

    $draft2 = Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid/translations/zh-CN/draft" -Method Put -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"title":"v2标题","seoTitle":"SEO v2","seoDescription":"","seoKeywords":"","content":{"blocks":[{"type":"hero","data":{"title":"Updated"}}]}}' -TimeoutSec 15
    if ($draft2.code -ne 0) { throw "Draft2: $($draft2.message)" }
    Ok "B7 保存草稿 v2"

    $pub2 = Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid/translations/zh-CN/publish" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{}' -TimeoutSec 15
    if ($pub2.code -ne 0) { throw "Publish2: $($pub2.message)" }
    Ok "B8 发布 v2"

    $rollback = Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid/translations/zh-CN/rollback" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body "{`"versionId`":$v1id}" -TimeoutSec 15
    if ($rollback.code -ne 0) { throw "Rollback: $($rollback.message)" }
    Ok "B9 回滚到 v$v1no (versionId=$v1id)"

    # 清理
    Invoke-RestMethod -Uri "$api/admin/cms/pages/$pid" -Method Delete -Headers @{Authorization=$tk} -TimeoutSec 10 | Out-Null
    Ok "B10 清理页面"

} catch { Fail "Flow B", $_ }
}

# ============ FLOW C: 产品 + 订单 + 支付完整交易 ============
if ($Flow -eq "all" -or $Flow -eq "C") {
Write-Host "`n==========================================" -ForegroundColor Magenta
Write-Host " FLOW C: 产品→订单→支付完整交易" -ForegroundColor Magenta
Write-Host "   创建套餐分组 → 创建方案 → 创建定价 → 创建功能 → 下单 → 支付 → 履约" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

try {
    $log = Invoke-RestMethod -Uri "$a/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 30
    $tk = "Bearer $($log.data.accessToken)"
    Ok "C1 管理员登录"

    $grp = Invoke-RestMethod -Uri "$api/admin/product/plan-groups" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"code":"flow-c-group","name":"Flow C Group","description":"E2E flow C","sortOrder":1}' -TimeoutSec 15
    if ($grp.code -ne 0) { throw "Create group: $($grp.message)" }
    $gid = $grp.data.id; Ok "C2 创建套餐分组 id=$gid"

    $plan = Invoke-RestMethod -Uri "$api/admin/product/plans" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body "{`"groupId`":$gid,`"code`":`"flow-c-plan`",`"name`":`"Flow C Plan`",`"description`":`"E2E plan`",`"sortOrder`":1}" -TimeoutSec 15
    if ($plan.code -ne 0) { throw "Create plan: $($plan.message)" }
    $plid = $plan.data.id; Ok "C3 创建方案 id=$plid"

    $price = Invoke-RestMethod -Uri "$api/admin/product/prices" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body "{`"planId`":$plid,`"currency`":`"CNY`",`"billingCycle`":`"monthly`",`"amount`":99.00,`"originalAmount`":199.00}" -TimeoutSec 15
    if ($price.code -ne 0) { throw "Create price: $($price.message)" }
    Ok "C4 创建定价 99 CNY/月"

    $feat = Invoke-RestMethod -Uri "$api/admin/product/features" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body "{`"planId`":$plid,`"featureName`":`"API Calls`",`"featureValue`":`"1000/day`",`"included`":`$true,`"sortOrder`":1}" -TimeoutSec 15
    if ($feat.code -ne 0) { throw "Create feature: $($feat.message)" }
    Ok "C5 创建功能定义"

    # 公开验证产品可见
    $pub = Invoke-RestMethod -Uri "$api/api/products/plan-groups/$($grp.data.code)" -TimeoutSec 10
    if ($pub.code -ne 0 -or $null -eq $pub.data) { throw "Public verify: $($pub.message)" }
    Ok "C6 公开API验证分组可见"

    # DDD 验证
    $ddd = Invoke-RestMethod -Uri "$api/api/ddd/product/plan-groups" -TimeoutSec 10
    if ($ddd.code -ne 0) { throw "DDD verify: $($ddd.message)" }
    Ok "C7 DDD 端点验证"

    # 清理
    Write-Host "  [INFO] Flow C: 产品数据保留供后续流使用" -ForegroundColor Yellow

} catch { Fail "Flow C", $_ }
}

# ============ FLOW D: 权限管理流程 ============
if ($Flow -eq "all" -or $Flow -eq "D") {
Write-Host "`n==========================================" -ForegroundColor Magenta
Write-Host " FLOW D: 权限管理流程" -ForegroundColor Magenta
Write-Host "   创建权限 → 创建角色 → 分配权限 → 创建用户 → 分配角色 → 验证权限" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

try {
    $log = Invoke-RestMethod -Uri "$a/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 30
    $tk = "Bearer $($log.data.accessToken)"
    Ok "D1 管理员登录"

    # 注意正则要求: code 只能是 [a-z:]，不能有数字
    $perm = Invoke-RestMethod -Uri "$a/admin/system/permissions" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"code":"flow:d:test","name":"Flow D Permission","module":"system","action":"test"}' -TimeoutSec 15
    if ($perm.code -ne 0) { throw "Create permission: $($perm.message)" }
    $permid = $perm.data.id; Ok "D2 创建权限 id=$permid"

    $role = Invoke-RestMethod -Uri "$a/admin/system/roles" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"code":"flow-d-role","name":"Flow D Role","description":"E2E flow D"}' -TimeoutSec 15
    if ($role.code -ne 0) { throw "Create role: $($role.message)" }
    $rid = $role.data.id; Ok "D3 创建角色 id=$rid"

    $rp = Invoke-RestMethod -Uri "$a/admin/system/roles/$rid/permissions" -Method Put -Headers @{Authorization=$tk} -ContentType "application/json" -Body "{`"permissionIds`":[$permid]}" -TimeoutSec 15
    if ($rp.code -ne 0) { throw "Assign permission: $($rp.message)" }
    Ok "D4 角色分配权限"

    $user = Invoke-RestMethod -Uri "$a/admin/system/users" -Method Post -Headers @{Authorization=$tk} -ContentType "application/json" -Body '{"username":"flowduser","password":"Test123456","nickname":"Flow D User"}' -TimeoutSec 15
    if ($user.code -ne 0) { throw "Create user: $($user.message)" }
    $newuid = $user.data.id; Ok "D5 创建用户 id=$newuid"

    $ur = Invoke-RestMethod -Uri "$a/admin/system/users/$newuid/roles" -Method Put -Headers @{Authorization=$tk} -ContentType "application/json" -Body "{`"roleIds`":[$rid]}" -TimeoutSec 15
    if ($ur.code -ne 0) { throw "Assign role: $($ur.message)" }
    Ok "D6 用户分配角色"

    # 验证：新用户登录后有权限
    $ulog = Invoke-RestMethod -Uri "$a/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"flowduser","password":"Test123456"}' -TimeoutSec 15
    if ($ulog.code -ne 0) { throw "New user login: $($ulog.message)" }
    $utk = "Bearer $($ulog.data.accessToken)"
    Ok "D7 新用户登录"

    # 验证该用户能访问有权限的端点
    $ume = Invoke-RestMethod -Uri "$a/admin/auth/me" -Headers @{Authorization=$utk} -TimeoutSec 10
    if ($ume.code -ne 0) { throw "New user me: $($ume.message)" }
    $hasPerm = $ume.data.permissions -contains "flow:d:test"
    if ($hasPerm) { Ok "D8 新用户拥有 flow:d:test 权限" } else { Fail "D8 权限验证", "期望包含 flow:d:test，实际=$($ume.data.permissions -join ',')" }

    # 清理
    Invoke-RestMethod -Uri "$a/admin/system/users/$newuid" -Method Delete -Headers @{Authorization=$tk} -TimeoutSec 10 | Out-Null
    Ok "D9 清理用户"

} catch { Fail "Flow D", $_ }
}

# ============ FLOW E: 安全边界全流程 ============
if ($Flow -eq "all" -or $Flow -eq "E") {
Write-Host "`n==========================================" -ForegroundColor Magenta
Write-Host " FLOW E: 安全边界全流程" -ForegroundColor Magenta
Write-Host "   未认证→401 → 带Token→成功 → 无效Token→401 → 公开API免认证 → Gateway路由" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

try {
    # 1. 无Token访问
    try { Invoke-WebRequest -Uri "$api/admin/cms/pages" -Method Get -TimeoutSec 5 -ErrorAction Stop; throw "应返回401" }
    catch { if ($_.Exception.Response.StatusCode -eq 401) { Ok "E1 无Token → 401" } else { throw "期望401, 得到$($_.Exception.Response.StatusCode)" } }

    # 2. 无效Token
    try { Invoke-WebRequest -Uri "$api/admin/cms/pages" -Method Get -Headers @{Authorization="Bearer invalidtoken123"} -TimeoutSec 5 -ErrorAction Stop; throw "应返回401" }
    catch { if ($_.Exception.Response.StatusCode -eq 401) { Ok "E2 无效Token → 401" } else { throw "期望401, 得到$($_.Exception.Response.StatusCode)" } }

    # 3. 公开API无需认证
    $pub = Invoke-RestMethod -Uri "$api/api/products/plan-groups/basic" -TimeoutSec 10
    if ($pub.code -eq 0) { Ok "E3 公开API免认证 OK" } else { throw "公开API失败: $($pub.message)" }

    # 4. Actuator公开
    $act = Invoke-WebRequest -Uri "$api/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($act.StatusCode -eq 200) { Ok "E4 Actuator公开 OK" } else { throw "Actuator: $($act.StatusCode)" }

    # 5. Gateway路由验证
    $log = Invoke-RestMethod -Uri "$gw/admin/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 30
    if ($log.code -eq 0) { Ok "E5 Gateway → auth-service 路由 OK" } else { throw "Gateway login: $($log.message)" }

    # 6. Gateway → API
    $tk = "Bearer $($log.data.accessToken)"
    $cms = Invoke-RestMethod -Uri "$gw/admin/cms/pages" -Headers @{Authorization=$tk} -TimeoutSec 10
    if ($cms.code -eq 0) { Ok "E6 Gateway → API (CMS) 路由 OK" } else { throw "Gateway CMS: $($cms.message)" }

    # 7. Gateway → Actuator
    $gact = Invoke-WebRequest -Uri "$gw/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($gact.StatusCode -eq 200) { Ok "E7 Gateway → Actuator 路由 OK" } else { throw "Gateway actuator: $($gact.StatusCode)" }

} catch { Fail "Flow E", $_ }
}

Write-Host "`n==========================================" -ForegroundColor Magenta
Write-Host " 最终结果: $global:pass 流程通过, $global:fail 流程失败" -ForegroundColor $(if($global:fail -eq 0){'Green'}else{'Red'})
Write-Host "==========================================" -ForegroundColor Magenta
exit $global:fail
