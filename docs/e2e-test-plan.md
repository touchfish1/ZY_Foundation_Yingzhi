# 全流程端到端测试方案

覆盖 6 个后端服务、84 个 API 端点，按业务流程组织。

---

## 前置条件

| 条件 | 说明 |
|------|------|
| 中间件运行中 | PostgreSQL / Redis / MinIO / Nacos（WSL Docker 常驻） |
| 后端全部启动 | `api:8088` `auth:8082` `system:8081` `user:8085` `order:8083` `payment:8084` `gateway:8080` |
| 种子数据 | 运行 `scripts/seed-test-data.ps1` 初始化 |

---

## 阶段一：认证与权限（auth-service）

验证认证、用户、角色、权限、菜单的完整 CRUD。

### 1.1 管理员登录

```
POST /admin/auth/login
Body: {"username":"admin","password":"admin123"}
→ 断言: code=0, data.accessToken 不为空
→ 保存 Token 供后续测试
```

### 1.2 获取当前用户与菜单

```
GET /admin/auth/me          → 当前登录用户信息
GET /admin/auth/menus       → 侧边菜单树
```

### 1.3 权限管理

```
GET    /admin/system/permissions?page=1&pageSize=20   → 权限列表
GET    /admin/system/permissions/modules              → 权限模块列表
POST   /admin/system/permissions                      → 创建权限
PUT    /admin/system/permissions/{id}                 → 编辑权限
DELETE /admin/system/permissions/{id}                 → 删除权限
```

### 1.4 角色管理

```
GET    /admin/system/roles?page=1&pageSize=20   → 角色列表
POST   /admin/system/roles                      → 创建角色
PUT    /admin/system/roles/{id}                 → 编辑角色
GET    /admin/system/roles/{id}/permissions     → 查看角色权限
PUT    /admin/system/roles/{id}/permissions     → 设置角色权限
DELETE /admin/system/roles/{id}                 → 删除角色
```

### 1.5 用户管理

```
GET    /admin/system/users?page=1&pageSize=20   → 用户列表（含 admin）
POST   /admin/system/users                      → 创建用户
PUT    /admin/system/users/{id}                 → 编辑用户
GET    /admin/system/users/{id}/roles           → 查看用户角色
PUT    /admin/system/users/{id}/roles           → 分配用户角色
DELETE /admin/system/users/{id}                 → 删除用户
```

### 1.6 菜单管理

```
GET    /admin/system/menus                      → 菜单树
POST   /admin/system/menus                      → 创建菜单项
PUT    /admin/system/menus/{id}                 → 编辑菜单
PUT    /admin/system/menus/sort                 → 排序
DELETE /admin/system/menus/{id}                 → 删除菜单
```

### 1.7 菜单管理

```
GET    /admin/system/menus                      → 菜单树
POST   /admin/system/menus                      → 创建菜单项
PUT    /admin/system/menus/{id}                 → 编辑菜单
PUT    /admin/system/menus/sort                 → 排序
DELETE /admin/system/menus/{id}                 → 删除菜单
```

### 1.8 DDD 公开端点

```
GET  /api/ddd/auth/users        → 用户列表
POST /api/ddd/auth/users        → 创建用户
GET  /api/ddd/auth/users/{id}   → 用户详情
GET  /api/ddd/auth/roles        → 角色列表
POST /api/ddd/auth/roles        → 创建角色
```

---

## 阶段二：产品管理（api - product 模块）

### 2.1 套餐分组

```
POST /admin/product/plan-groups    → 创建分组 {code, name, description, sortOrder}
GET   /admin/product/plan-groups   → 分组列表
```

### 2.2 套餐方案

```
POST /admin/product/plans    → 创建方案 {groupId, code, name, description, sortOrder}
GET   /admin/product/plans   → 方案列表
```

### 2.3 定价

```
POST /admin/product/prices    → 创建定价 {planId, currency, billingCycle, amount, originalAmount}
GET   /admin/product/prices   → 定价列表
```

### 2.4 功能定义

```
POST /admin/product/features    → 创建功能 {planId, featureName, featureValue, included, sortOrder}
```

### 2.5 公开/DDD 端点

```
GET  /api/products/plan-groups/{code}     → 按 code 查分组（公开）
GET  /api/products/plans                  → 方案列表（公开）
GET  /api/ddd/product/plan-groups         → DDD 分组列表
GET  /api/ddd/product/plan-groups/{code}  → DDD 按 code 查
POST /api/ddd/product/plan-groups         → DDD 创建分组
```

---

## 阶段三：CMS 内容管理（api - cms 模块）

核心流程：创建页面 → 编辑草稿 → 发布 → 渲染。

### 3.1 页面管理

```
GET    /admin/cms/pages                     → 页面列表
POST   /admin/cms/pages                     → 创建页面 {slug, defaultLocale, title}
GET    /admin/cms/pages/{id}                → 页面详情
PUT    /admin/cms/pages/{id}                → 编辑页面基本属性
DELETE /admin/cms/pages/{id}                → 删除页面
```

### 3.2 草稿编辑 → 发布

```
PUT    /admin/cms/pages/{id}/translations/{locale}/draft    → 保存草稿
GET    /admin/cms/pages/{id}/translations/{locale}/draft    → 读取草稿
POST   /admin/cms/pages/{id}/translations/{locale}/publish  → 发布（生成快照）
```

### 3.3 版本管理

```
GET  /admin/cms/pages/{id}/translations/{locale}/versions   → 版本历史列表
POST /admin/cms/pages/{id}/translations/{locale}/rollback   → 回滚到指定版本
GET  /admin/cms/pages/{id}/preview?locale=&versionId=       → 预览指定版本
```

### 3.4 公开渲染

```
GET /api/cms/pages/render?path=/&locale=zh-CN   → 渲染已发布页面（含 blocks）
```

### 3.5 区块定义

```
GET /admin/cms/block-definitions   → 所有区块类型定义列表
```

### 3.6 DDD 端点

```
GET    /api/ddd/cms/pages       → 页面列表
POST   /api/ddd/cms/pages       → 创建页面
GET    /api/ddd/cms/pages/{id}  → 页面详情
DELETE /api/ddd/cms/pages/{id}  → 删除页面
```

---

## 阶段四：订单与用量（api + order-service）

### 4.1 创建订单（公开）

```
POST /api/orders    → 创建订单 {userId, planId, quantity, ...}
GET  /api/orders/{orderNo}   → 查询订单
```

### 4.2 管理员查看订单

```
GET /admin/orders   → 订单列表
```

### 4.3 DDD 订单端点

```
POST /api/ddd/orders                → 创建订单
GET  /api/ddd/orders/{orderNo}      → 查询订单
GET  /api/ddd/orders                → 订单列表
```

### 4.4 用量记录与订阅

```
GET  /api/usage/{userId}?page=1&pageSize=20            → 用量记录
GET  /api/usage/{userId}/summary?start=&end=           → 用量汇总
POST /api/usage/record                                 → 记录用量
GET  /api/subscriptions/active?userId=                 → 活跃订阅
GET  /api/subscriptions?userId=                        → 全部订阅
```

### 4.5 订单履约

```
POST /api/orders/{orderNo}/fulfill   → 订单履约
```

---

## 阶段五：支付（api + payment-service）

### 5.1 发起支付

```
POST /api/payments/checkout   → 发起支付 {orderNo, paymentMethod}
```

### 5.2 模拟支付回调

```
POST /api/payments/mock/{paymentNo}/success   → 模拟支付成功
```

### 5.3 管理员查看支付

```
GET /admin/payments   → 支付列表（api + payment-service 两端）
```

### 5.4 DDD 支付端点

```
GET /api/ddd/payments/{paymentNo}   → 查询支付
GET /api/ddd/payments               → 支付列表
```

---

## 阶段六：资产与文件（api - asset 模块）

### 6.1 文件上传

```
POST /admin/assets/files   → multipart/form-data 上传文件
GET   /admin/assets/files  → 文件列表
```

### 6.2 DDD 资产端点

```
GET /api/ddd/assets   → 资产列表
```

---

## 阶段七：系统设置（system-service）

### 7.1 健康检查

```
GET /api/system/ping   → 服务存活 + 时间戳
```

### 7.2 系统设置

```
GET  /api/system/settings                → 公开设置（Map）
GET  /admin/system/settings              → 管理端设置列表
PUT  /admin/system/settings              → 批量更新设置
PUT  /admin/system/settings/{key}        → 更新单条设置
```

### 7.3 DDD 设置端点

```
GET  /api/ddd/settings          → 设置列表
PUT  /api/ddd/settings/{key}    → 更新设置
```

### 7.4 监控

```
GET /admin/system/monitor/stats   → JVM/系统状态
```

### 7.5 用户代理

```
GET  /admin/users?page=1&pageSize=20     → 通过 Feign 代理到 user-service
GET  /admin/users/{id}                   → 用户详情
PUT  /admin/users/{id}/status?status=    → 更新状态
```

---

## 阶段八：SaaS 用户（user-service）

### 8.1 注册与登录

```
POST /api/auth/register    → 注册 {email, password, nickname}
POST /api/auth/login       → 登录 {email, password}
```

### 8.2 用户信息

```
GET /api/auth/profile            → 当前用户信息（需 token）
GET /api/auth/verify-key?apiKey= → 验证 API Key
```

### 8.3 余额

```
GET  /api/balance/{userId}                  → 余额查询
GET  /api/balance/{userId}/transactions     → 交易记录
POST /api/balance/{userId}/recharge         → 充值
```

### 8.4 审计日志

```
GET /api/logs/user/{userId}?page=1&pageSize=20   → 用户操作日志
GET /api/logs?page=1&pageSize=20                  → 全部日志
```

---

## 阶段九：安全边界验证

### 9.1 未认证访问

```
GET  /admin/cms/pages             → 401 {code:401, message:"未登录，请先登录"}
GET  /admin/product/plan-groups   → 401
GET  /admin/system/users          → 401
```

### 9.2 公开端点免认证

```
GET  /api/cms/pages/render?path=/   → 200
GET  /api/products/plan-groups/{code} → 200
GET  /actuator/health               → 200
```

### 9.3 无效 Token

```
GET  /admin/cms/pages  Authorization: Bearer invalid_token
→ 401
```

### 9.4 无权限 Token（使用普通用户 Token）

```
POST /admin/system/permissions  → 403（普通用户无 system:permission:create 权限）
```

---

## 阶段十：Gateway 路由验证

### 10.1 通过 Gateway 访问各服务

```
Gateway → auth-service:   POST /admin/auth/login
Gateway → auth-service:   GET  /admin/system/users
Gateway → system-service: GET  /admin/system/monitor/stats
Gateway → api:            GET  /admin/cms/pages
Gateway → api:            GET  /api/cms/pages/render
Gateway → order-service:  GET  /api/orders/{orderNo}
Gateway → payment-service: POST /api/payments/checkout
Gateway → user-service:   POST /api/auth/login
Gateway → api:            GET  /actuator/health
```

### 10.2 直连验证（绕过 Gateway）

```
API 8088:           GET  /api/cms/pages/render
Auth 8082:          POST /admin/auth/login
System 8081:        GET  /api/system/ping
User 8085:          POST /api/auth/login
Order 8083:         GET  /api/orders/{orderNo}
Payment 8084:       POST /api/payments/checkout
```

---

## 执行策略

**建议分 3 轮执行：**

| 轮次 | 范围 | 目标 |
|------|------|------|
| 第 1 轮 | 阶段一 + 九 + 十 | 核心认证 + 安全 + 路由 |
| 第 2 轮 | 阶段二 + 三 + 四 | 业务主体（产品→CMS→订单） |
| 第 3 轮 | 阶段五 + 六 + 七 + 八 | 支付 + 资产 + 系统 + 用户 |

每轮间独立，可并行执行。

---

## 验证清单

每条测试用例通过标准：

- [ ] HTTP 状态码符合预期（200/201/401/403 等）
- [ ] 响应符合统一格式 `{code, message, data}`
- [ ] 分页接口 `data` 包含 `{items, page, pageSize, total}`
- [ ] 创建操作返回 201 且 `data.id` 不为空
- [ ] 删除后再次查询返回空或 404
- [ ] 更新后内容与实际一致
- [ ] 认证失败返回 `code:401`，权限不足返回 `code:403`
