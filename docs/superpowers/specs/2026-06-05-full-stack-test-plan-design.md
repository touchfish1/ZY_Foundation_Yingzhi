# 全栈测试流程设计

## 背景

掌元盈智平台是一个多模块单体仓库，包含 7 个后端微服务 + Vue 3 管理后台 + Nuxt 3 前台站点。现有测试覆盖了单元/领域测试层面，缺少端到端集成测试和前端功能验证。本设计覆盖所有服务的完整功能验证。

## 测试层级

| 层级 | 范围 | 工具 | 数量 |
|------|------|------|------|
| L1 冒烟测试 | 服务启动 + 健康 + 登录 | curl 脚本 | 1 |
| L2 后端 API 集成测试 | 全部 REST 端点 | curl + jq | 7 模块 |
| L3 跨服务业务流程 | 完整用户操作路径 | curl + bash | 5 流程 |
| L4 前端构建检查 | 类型检查 + 构建成功 | vue-tsc / nuxt typecheck | 2 |
| L5 前端页面冒烟 | 页面可访问 + 关键交互 | curl + playwright (可选) | 2 |

---

## L1：冒烟测试（服务启动验证）

**前置条件：** 所有服务已启动

```bash
# 1. 后端服务存活检查
curl -sf http://localhost:8088/actuator/health | jq .status  # → "UP"
curl -sf http://localhost:8082/admin/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .code  # → 0
curl -sf http://localhost:8081/api/system/ping | jq .  # → 任意 200 响应
curl -sf http://localhost:8080/actuator/health | jq .status  # → 200

# 2. 前端服务存活检查
curl -sf http://localhost:5173/ | head -5  # → HTML 内容
curl -sf http://localhost:3000/ | head -5  # → HTML 内容

# 3. 数据库连接验证
curl -sf http://localhost:8088/admin/cms/pages \
  -H "Authorization: Bearer $TOKEN" | jq .code  # → 0
```

---

## L2：后端 API 集成测试（按模块）

### 模块 A：认证与权限 (auth-service, 端口 8082)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| A01 | /admin/auth/login | POST | 正确账号密码登录 | code=0, 返回 accessToken |
| A02 | /admin/auth/login | POST | 错误密码 | code=401 或 非0 |
| A03 | /admin/auth/login | POST | 空请求体 | 400/Bad Request |
| A04 | /admin/auth/me | GET | 携带有效 Token | 返回当前用户信息 |
| A05 | /admin/auth/me | GET | 无 Token | 401 |
| A06 | /admin/auth/me | GET | 无效 Token | 401 |
| A07 | /admin/auth/menus | GET | 携带 Token 获取菜单 | 返回菜单树 |
| A08 | /admin/system/users | GET | 用户列表 | 返回分页用户 |
| A09 | /admin/system/users | POST | 创建用户 | 创建成功 |
| A10 | /admin/system/users/{id} | PUT | 更新用户 | 更新成功 |
| A11 | /admin/system/roles | GET | 角色列表 | 返回角色列表 |
| A12 | /admin/system/roles | POST | 创建角色 | 创建成功 |
| A13 | /admin/system/roles/{id}/permissions | PUT | 分配权限 | 成功 |
| A14 | /admin/system/permissions | GET | 权限列表 | 返回权限 |
| A15 | /admin/system/permissions/modules | GET | 按模块分组 | 返回模块分组权限 |
| A16 | /admin/system/menus | GET | 菜单树 | 返回菜单列表 |
| A17 | /admin/system/menus | POST | 创建菜单项 | 创建成功 |
| A18 | /admin/system/menus/{id} | PUT | 更新菜单 | 更新成功 |
| A19 | /admin/system/menus/{id} | DELETE | 删除菜单 | 删除成功 |
| A20 | /admin/system/menus/sort | PUT | 排序菜单 | 成功 |

### 模块 B：CMS (api, 端口 8088)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| B01 | /admin/cms/pages | GET | 页面列表 | 返回页面数组 |
| B02 | /admin/cms/pages | POST | 创建页面 | 创建成功 |
| B03 | /admin/cms/pages/{id} | GET | 获取页面详情 | 返回页面详情 |
| B04 | /admin/cms/pages/{id} | PUT | 更新页面 | 更新成功 |
| B05 | /admin/cms/pages/{id} | DELETE | 删除页面 | 删除成功 |
| B06 | /admin/cms/pages/{id}/translations/{locale}/draft | PUT | 保存草稿 | 成功 |
| B07 | /admin/cms/pages/{id}/translations/{locale}/draft | GET | 获取草稿 | 返回草稿内容 |
| B08 | /admin/cms/pages/{id}/translations/{locale}/versions | GET | 版本列表 | 返回版本列表 |
| B09 | /admin/cms/pages/{id}/translations/{locale}/publish | POST | 发布页面 | 成功 |
| B10 | /admin/cms/pages/{id}/translations/{locale}/rollback | POST | 回滚版本 | 成功 |
| B11 | /admin/cms/pages/{id}/preview | GET | 预览页面 | 返回预览数据 |
| B12 | /admin/cms/block-definitions | GET | 区块定义列表 | 返回区块定义 |
| B13 | /api/cms/pages/render?slug=/ | GET | 前台渲染首页 | 返回渲染内容 |
| B14 | /api/cms/pages/list | GET | 前台页面列表 | 返回公开页面列表 |
| B15 | /api/ddd/cms/pages | GET | DDD 页面列表 | 返回页面列表 |
| B16 | /api/ddd/cms/pages | POST | DDD 创建页面 | 创建成功 |
| B17 | /api/ddd/cms/pages/{id} | GET | DDD 获取页面 | 返回页面详情 |
| B18 | /api/ddd/cms/pages/{id} | DELETE | DDD 删除页面 | 删除成功 |

### 模块 C：资产管理 (api)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| C01 | /admin/assets/files | GET | 文件列表 | 返回文件列表 |
| C02 | /admin/assets/files | POST | 上传文件 | 上传成功 |
| C03 | /api/ddd/assets | GET | DDD 资产列表 | 返回资产列表 |

### 模块 D：产品与套餐 (api)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| D01 | /admin/product/plan-groups | GET | 套餐组列表 | 返回列表 |
| D02 | /admin/product/plan-groups | POST | 创建套餐组 | 创建成功 |
| D03 | /admin/product/plans | GET | 套餐列表 | 返回列表 |
| D04 | /admin/product/plans | POST | 创建套餐 | 创建成功 |
| D05 | /admin/product/prices | GET | 价格列表 | 返回列表 |
| D06 | /admin/product/prices | POST | 创建价格 | 创建成功 |
| D07 | /admin/product/features | POST | 创建特性 | 创建成功 |
| D08 | /api/products/plan-groups/{code} | GET | 公开套餐组 | 返回详情 |
| D09 | /api/products/plans | GET | 公开套餐列表 | 返回列表 |
| D10 | /api/ddd/product/plan-groups | GET | DDD 套餐组列表 | 返回列表 |
| D11 | /api/ddd/product/plan-groups/{code} | GET | DDD 套餐组详情 | 返回详情 |
| D12 | /api/ddd/product/plan-groups | POST | DDD 创建套餐组 | 创建成功 |

### 模块 E：订单 (api + order-service)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| E01 | /api/orders | POST | 创建订单 | 创建成功 |
| E02 | /api/orders/{orderNo} | GET | 查询订单 | 返回订单详情 |
| E03 | /api/orders | GET | 订单列表 | 返回列表 |
| E04 | /admin/orders | GET | 管理端订单列表 | 返回列表 |

### 模块 F：支付 (api + payment-service)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| F01 | /api/payments/checkout | POST | 创建支付 | 返回支付信息 |
| F02 | /api/payments/mock/{paymentNo}/success | POST | 模拟支付成功 | 支付状态更新 |
| F03 | /api/ddd/payments/{paymentNo} | GET | DDD 查询支付 | 返回支付详情 |
| F04 | /api/ddd/payments | GET | DDD 支付列表 | 返回列表 |
| F05 | /admin/payments | GET | 管理端支付列表 | 返回列表 |
| F06 | /admin/payments/{paymentNo} | GET | 管理端支付详情 | 返回详情 |

### 模块 G：系统设置 (system-service)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| G01 | /admin/system/settings | GET | 获取设置列表 | 返回设置 |
| G02 | /admin/system/settings | PUT | 批量更新设置 | 更新成功 |
| G03 | /admin/system/settings/{key} | PUT | 更新单个设置 | 更新成功 |
| G04 | /admin/system/monitor/stats | GET | 系统监控统计 | 返回统计数据 |
| G05 | /api/system/settings | GET | 公开设置 | 返回公开设置 |
| G06 | /api/system/ping | GET | 健康检查 | 返回 pong |
| G07 | /api/ddd/settings | GET | DDD 设置列表 | 返回设置 |
| G08 | /api/ddd/settings/{key} | PUT | DDD 更新设置 | 更新成功 |

### 模块 H：用户服务 (user-service, 通过 gateway)

| ID | 端点 | 方法 | 测试场景 | 预期 |
|----|------|------|----------|------|
| H01 | /api/auth/register | POST | SaaS 用户注册 | 注册成功 |
| H02 | /api/auth/login | POST | SaaS 用户登录 | 返回 token |
| H03 | /api/auth/profile | GET | 获取用户信息 | 返回个人资料 |
| H04 | /api/auth/keys/regenerate | PUT | 重新生成 API Key | 成功 |
| H05 | /api/auth/verify-key | GET | 验证 API Key | 返回验证结果 |
| H06 | /api/balance/{userId} | GET | 查询余额 | 返回余额 |
| H07 | /api/balance/{userId}/recharge | POST | 充值 | 充值成功 |
| H08 | /api/logs | GET | 审计日志列表 | 返回列表 |

---

## L3：跨服务业务流程测试

### 流程 1：完整用户注册 → 登录 → 操作链

```
1. POST /admin/auth/login → 获取 admin Token
2. GET  /admin/auth/me → 验证身份
3. POST /admin/system/users → 创建新用户
4. PUT  /admin/system/users/{id}/roles → 分配角色
5. 新用户 Token 下 GET /admin/auth/menus → 验证权限
```

### 流程 2：CMS 内容发布全流程

```
1. POST /admin/cms/pages → 创建页面
2. PUT  /admin/cms/pages/{id}/translations/zh-CN/draft → 保存草稿
3. GET  /admin/cms/pages/{id}/translations/zh-CN/draft → 验证草稿
4. POST /admin/cms/pages/{id}/translations/zh-CN/publish → 发布
5. GET  /api/cms/pages/render?slug={slug} → 前台验证渲染
6. POST /admin/cms/pages/{id}/translations/zh-CN/rollback → 回滚版本
7. DELETE /admin/cms/pages/{id} → 清理
```

### 流程 3：产品 → 订单 → 支付链路

```
1. POST /admin/product/plan-groups → 创建套餐组
2. POST /admin/product/plans → 创建套餐
3. POST /admin/product/prices → 创建定价
4. POST /api/orders → 创建订单
5. GET  /api/orders/{orderNo} → 验证订单
6. POST /api/payments/checkout → 创建支付
7. POST /api/payments/mock/{paymentNo}/success → 模拟支付完成
8. GET  /admin/orders → 管理端查看订单
9. GET  /admin/payments/{paymentNo} → 管理端查看支付
```

### 流程 4：系统设置管理流程

```
1. GET  /admin/system/settings → 获取当前设置
2. PUT  /admin/system/settings/{key} → 更新 footer_text
3. GET  /api/system/settings → 公开接口验证
4. GET  /admin/system/monitor/stats → 查看系统统计
```

### 流程 5：SaaS 用户完整生命周期

```
1. POST /api/auth/register → 注册用户
2. POST /api/auth/login → 登录获取 Token
3. GET  /api/auth/profile → 查看个人资料
4. PUT  /api/auth/password → 修改密码
5. PUT  /api/auth/keys/regenerate → 生成 API Key
6. GET  /api/auth/verify-key → 验证 Key
7. POST /api/balance/{userId}/recharge → 充值余额
8. GET  /api/balance/{userId} → 查看余额
9. GET  /api/balance/{userId}/transactions → 查看交易记录
```

---

## L4：前端构建检查

```bash
# Admin 前端类型检查
cd frontend/admin && npx vue-tsc -b --noEmit

# Web 前端类型检查
cd frontend/web && npx nuxt typecheck

# Admin 前端构建
cd frontend/admin && npm run build

# Web 前端构建
cd frontend/web && npm run build
```

---

## L5：前端页面冒烟测试

### Admin 管理后台

| ID | 页面 | 验证方式 |
|----|------|----------|
| FE01 | /login | 页面加载，输入 admin/admin123 登录成功 |
| FE02 | / (Dashboard) | 登录后默认页，数据卡片展示 |
| FE03 | /assets | 文件资产管理列表 |
| FE04 | /cms/pages | CMS 页面列表 |
| FE05 | /products/plan-groups | 套餐组管理 |
| FE06 | /products/plans | 套餐管理 |
| FE07 | /orders | 订单列表 |
| FE08 | /payments/transactions | 支付交易列表 |
| FE09 | /system/users | 用户管理 |
| FE10 | /system/roles | 角色管理 |
| FE11 | /system/permissions | 权限管理 |
| FE12 | /system/settings | 系统设置 |
| FE13 | /system/menus | 菜单管理 |
| FE14 | /system/monitor | 系统监控 |

### Web 前台站点

| ID | 页面 | URL | 验证方式 |
|----|------|-----|----------|
| FW01 | 首页 | / | 页面渲染，动画组件展示 |
| FW02 | 定价 | /pricing | 套餐展示 |
| FW03 | 登录 | /login | SaaS 登录页 |
| FW04 | 注册 | /register | SaaS 注册页 |
| FW05 | 文档 | /docs | 文档页面 |
| FW06 | CMS 动态页 | /{slug} | CMS 渲染内容 |

---

## L6：Gateway 集成测试

所有 L2 测试通过 Gateway (端口 8080) 执行一遍，验证路由转发正确：

```bash
# 每个 L2 测试替换端口为 8080，验证：
# 1. auth-service 路由: /admin/auth/**, /admin/system/users/**, /admin/system/roles/**, ...
# 2. system-service 路由: /admin/system/settings/**, /api/system/**, ...
# 3. api 路由: /admin/cms/**, /admin/assets/**, /api/cms/**, ...
# 4. order-service 路由: /api/orders/**, /admin/orders/**, ...
# 5. payment-service 路由: /api/payments/**, /admin/payments/**, ...
# 6. user-service 路由: /api/auth/**, /api/balance/**, ...
```

---

## 测试脚本结构

```
tests/
├── integration/
│   ├── run-all.sh                  # 主入口：按顺序执行 L1→L2→L3→L6
│   ├── L1-smoke.sh                 # 冒烟测试
│   ├── L2-api/
│   │   ├── A-auth.sh               # 认证模块测试
│   │   ├── B-cms.sh                # CMS 模块测试
│   │   ├── C-asset.sh              # 资产管理测试
│   │   ├── D-product.sh            # 产品套餐测试
│   │   ├── E-order.sh              # 订单模块测试
│   │   ├── F-payment.sh            # 支付模块测试
│   │   ├── G-system.sh             # 系统设置测试
│   │   └── H-user.sh               # 用户服务测试
│   ├── L3-flow/
│   │   ├── F1-user-lifecycle.sh     # 用户生命周期
│   │   ├── F2-cms-publish.sh        # CMS 发布流程
│   │   ├── F3-order-payment.sh      # 订单支付链路
│   │   ├── F4-settings.sh           # 设置管理流程
│   │   └── F5-saas-lifecycle.sh     # SaaS 用户流程
│   ├── L5-frontend.sh              # 前端页面冒烟
│   ├── L6-gateway.sh               # Gateway 路由验证
│   └── lib/
│       ├── common.sh                # 公共函数（登录、断言、报告）
│       └── test-data.sql            # 测试数据初始化
├── unit/                            # 已有 JUnit 测试（保持现有）
└── CI.md                            # CI 集成说明
```

---

## 公共测试库 (lib/common.sh)

```bash
#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
ADMIN_TOKEN=""
PASS=0
FAIL=0
ERRORS=()

login() {
  local resp=$(curl -sf "$GATEWAY_URL/admin/auth/login" \
    -X POST -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
  ADMIN_TOKEN=$(echo "$resp" | jq -r '.data.accessToken')
  if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "FATAL: Cannot obtain admin token"
    exit 1
  fi
}

assert_code() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    PASS=$((PASS+1))
    echo "  ✅ $label: code=$actual"
  else
    FAIL=$((FAIL+1))
    ERRORS+=("$label: expected code=$expected, got=$actual")
    echo "  ❌ $label: expected code=$expected, got=$actual"
  fi
}

assert_has_field() {
  local label="$1" field="$2" json="$3"
  local val=$(echo "$json" | jq -r ".$field // empty")
  if [ -n "$val" ] && [ "$val" != "null" ]; then
    PASS=$((PASS+1))
    echo "  ✅ $label: $field=$val"
  else
    FAIL=$((FAIL+1))
    ERRORS+=("$label: missing field $field")
    echo "  ❌ $label: missing field $field"
  fi
}

summary() {
  echo ""
  echo "=============================="
  echo "  PASSED: $PASS"
  echo "  FAILED: $FAIL"
  echo "=============================="
  if [ ${#ERRORS[@]} -gt 0 ]; then
    echo "  Errors:"
    for e in "${ERRORS[@]}"; do echo "    - $e"; done
  fi
  [ $FAIL -eq 0 ] && exit 0 || exit 1
}
```

---

## 执行顺序与依赖

```
L1 (冒烟) → L2 (API 集成) → L3 (业务流程) → L4 (前端构建) → L5 (前端冒烟) → L6 (Gateway)
                    ↑                                    ↑
              直接端口 (8088/8082等)                前端端口 (5173/3000)
```

- L1 失败则终止全部
- L2 各模块可并行执行
- L3 依赖 L2 的数据（如创建的套餐、页面）
- L4 独立，可在任何时候运行
- L5 需要 L4 构建成功
- L6 使用 Gateway URL，在 L2 通过后运行

---

## CI 集成

在 `.github/workflows/ci.yml` 中增加 `test-integration` job：

```yaml
test-integration:
  runs-on: ubuntu-latest
  needs: [test-api]  # 依赖现有 API 单元测试
  services:
    postgres:
      image: postgres:16
      env: { POSTGRES_DB: zhangyuan, POSTGRES_USER: zhangyuan, POSTGRES_PASSWORD: zhangyuan }
    redis:
      image: redis:7
  steps:
    - uses: actions/checkout@v4
    - name: Setup JDK 21 & Node 22
    - name: Start all services
    - name: Run L1 smoke tests
    - name: Run L2 API integration tests
    - name: Run L3 business flow tests
    - name: Run L6 Gateway tests
```