# Project ZHANGYUAN Skills 能力沉淀

> **来源项目**：ZY_Foundation_Yingzhi — AI 模型 API 服务平台  
> **技能类型**：业务技能 / 后端开发技能 / 测试技能 / 优化运维技能  
> **编制人**：专业测试架构师  
> **编制日期**：2026-06-06  

---

## 一、业务技能

### B1. AI API 服务平台通用模型

```
模式：统一API接入 → 套餐体系 → 用户认购 → 用量监控 → 计费结算
                             ↓
                        CMS内容管理 (官网/文档/博客)
                             ↓
                      RBAC管理后台 (运营/客服/财务)
```

| 技能ID | 技能名称 | 详细描述 | 复用价值 |
|--------|---------|---------|---------|
| B-001 | SaaS 套餐-订单-支付-订阅-用量闭环设计 | 从套餐定义(方案组/方案/价格/权益) → 用户选购 → 订单创建 → 支付处理 → 订阅激活 → API调用 → 用量扣减 → 订阅到期的全生命周期设计模式 | 高 — 标准 SaaS 计费模型，可直接复用至其他 API 平台或订阅类产品 |
| B-002 | 双用户体系设计 | SaaS 终端用户(API消费者) + 管理员用户(运营者)两套独立的认证/授权体系，通过 Gateway 路径前缀(api/ vs admin/)自动路由 | 中 — 适用于所有含管理后台的 SaaS 产品 |
| B-003 | CMS 驱动的前端内容管理 | 8种内置区块类型(Hero/Pricing/FAQ/CTA/FeatureGrid/Stats/RichText/ImageBanner)，支持多语言(zh-CN/en-US/ja-JP)、版本管理、草稿/发布/回滚流程 | 高 — 通用 CMS 模式，适配官网/文档站/博客等多场景 |
| B-004 | API Key + Token 双鉴权模式 | 终端用户通过 Token (Session) 访问控制台，通过 API Key (永久凭证) 调用 API；管理员通过独立 Token 访问后台 | 高 — 标准 API 平台的鉴权模式 |

---

### B2. 业务域知识地图

```
AI API 服务平台
├── 用户侧
│   ├── 注册 → 登录 → 控制台
│   ├── 浏览定价 → 选套餐 → 下单 → 支付
│   ├── 获取 API Key → 调用 API → 查看用量
│   └── 充值 → 消费 → 开发票
├── 运营侧
│   ├── CMS (页面/多语言/版本/发布)
│   ├── 产品 (方案组/方案/价格/权益)
│   ├── 订单 & 支付管理
│   ├── 用户 & 余额管理
│   └── 系统设置 & 监控
└── 管理侧
    ├── RBAC (用户/角色/权限/菜单)
    ├── 审计日志
    └── 媒体资源
```

---

## 二、后端开发技能

### B3. 微服务架构最佳实践

| 技能ID | 技能名称 | 要点 | 本项目的实现 |
|--------|---------|------|------------|
| DEV-001 | DDD 六边形架构落地 | domain/model 纯 POJO、adapter/in/rest → application → domain 单向依赖 | 严格遵循，ArchUnit 测试保障 |
| DEV-002 | Spring Cloud Gateway 路由拆分 | 按路径前缀分流到不同微服务，Gateway 层做统一鉴权和跨域 | 20条路由规则，按业务域拆分 |
| DEV-003 | BFF 聚合模式 | API 模块作为 Backend For Frontend，承接前端请求并聚合多服务数据 | api 模块(BFF) + 微服务独立模块 |
| DEV-004 | Feign + Nacos 服务间调用 | OpenFeign 声明式调用 + Nacos 服务发现，RestTemplate 作为备用 | 6组 Feign 调用关系 |
| DEV-005 | Flyway + JPA 数据库版本管理 | Flyway 管理 schema 变更，JPA 管理实体关系，ddl-auto 策略 | validate(api)/update(其他) |
| DEV-006 | Sa-Token RBAC 权限系统 | 权限码格式 module:resource:action，三级菜单(group/page/button) | admin_permission + admin_menu + 中间表 |

### DEV-001 示例：DDD 分层代码规范

```
com.zhangyuan.{service}
├── domain/
│   ├── model/          # 纯 POJO，无 JPA/Spring 注解
│   ├── repository/     # 接口，定义仓储契约
│   └── service/        # 纯业务规则，不依赖框架
├── application/
│   └── service/        # 编排用例，调用 domain service + 仓储
├── adapter/
│   ├── in/rest/        # @RestController，最薄的一层
│   └── out/persistence/ # JPA Entity + Repository 实现
├── dto/                # 数据传输对象
└── common/             # 工具类
```

---

### B4. 数据库设计模式

| 技能ID | 技能名称 | 适用场景 |
|--------|---------|---------|
| DB-001 | JSONB 快照模式 | `order_main.snapshot_json` — 订单锁定下单时的套餐价格权益快照，防止后续变更影响已支付订单 |
| DB-002 | KV 设置表模式 | `system_setting(key, value)` — 适用配置项不确定/动态扩展的场景 |
| DB-003 | 发布版本链模式 | `cms_page → translation → version(version_no递增) + publish_record` — 内容管理类系统通用版本方案 |
| DB-004 | 每日汇总表模式 | `usage_daily_summary(user_id, date, total_*) — 用空间换时间，避免每次查询实时聚合大量明细数据 |

---

## 三、测试技能

### T1. 微服务接口测试 checklist

| 阶段 | 检查项 | 说明 |
|------|--------|------|
| **入参校验** | 必填参数缺失 | 400/422 + 明确的错误消息 |
| | 参数类型错误 | string→int 转换失败 |
| | 参数长度越界 | @Size/@Max/@Min |
| | SQL注入尝试 | `' OR 1=1 --` |
| | XSS 尝试 | `<script>alert(1)</script>` |
| **鉴权校验** | 无 Token 访问 | 401 |
| | 过期 Token | 401 |
| | 跨角色 Token | SaaS Token 访问 admin 接口 → 401/403 |
| | 权限不足 | 有 Token 但无对应 permission → 403 |
| **业务逻辑** | 正向全流程 | 从创建到完成的完整业务链路 |
| | 重复操作 | 幂等性 (重复支付/重复注册/重复提交) |
| | 状态机流转 | 非法状态转换(如已取消的订单再支付) |
| | 数据一致性 | 并发下的乐观锁/悲观锁 |
| | 外键/关联校验 | 删除前检查引用 |
| **数据边界** | 金额精度 | decimal 类型，使用 BigDecimal |
| | 分页边界 | page=0/page=-1/pageSize=0/pageSize=10000 |
| | 唯一约束 | 重复 email/slug/code 的友好提示 |
| **跨服务** | Feign 调用超时 | 熔断/降级 |
| | 下游服务不可用 | 优雅降级而非级联失败 |

### T2. SpringCloud 微服务测试清单

```yaml
# 每个微服务独立测试项
单元测试:
  - Domain 模型: 纯 POJO 无依赖，TDD 友好
  - Domain Service: 纯业务逻辑
  - Application Service: mock Repository

集成测试:
  - Repository: @DataJpaTest + 嵌入式 DB
  - Controller: @WebMvcTest + mock Service
  - 全链: @SpringBootTest + TestRestTemplate

契约测试:
  - Feign 接口: Spring Cloud Contract / WireMock

端到端:
  - Gateway → 微服务 → DB 全流程
  - 跨服务业务流 (下单→支付→订阅)
```

### T3. 本项目发现缺陷的 5 大模式

```
模式1：缺乏幂等性防护 → P1 ×3
  复现：重复回调/重复提交
  排查：看回调接口第一行是否做了状态检查

模式2：物理删除未级联 → P1 ×2
  复现：删除主表记录后关联表产生孤立数据
  排查：查看 DELETE 方法是否有先查询关联表

模式3：金额计算无并发保护 → P1 ×1
  复现：高并发扣款超扣
  排查：检查 @Version 字段 / 原子 SQL

模式4：环境隔离缺失 → P0 ×1
  复现：测试接口在生产环境可用
  排查：检查 @Profile 条件 / Gateway 路由过滤

模式5：缺少应用层参数校验 → P2 ×6
  复现：依赖数据库约束而非应用层校验
  排查：查看 DTO 是否有 @NotNull/@Size/@Pattern
```

---

## 四、优化运维技能

### O1. 性能优化清单

| 场景 | 当前状态 | 优化建议 |
|------|---------|---------|
| CMS 页面渲染 | Redis 缓存快照，Nginx 托管静态资源 | ✅ 已优化 |
| 订单号生成 | 未明确 | 建议使用雪花算法(Snowflake)确保全局唯一、趋势递增 |
| 用量统计查询 | 日汇总表避免实时聚合 | ✅ 已优化 |
| 数据库连接池 | 默认 HikariCP | 建议监控连接池使用率，根据并发调整 maximum-pool-size |
| Redis 缓存 | CMS 快照 | 扩大缓存范围：套餐列表/系统设置也可缓存 |

### O2. 安全加固清单

| 安全项 | 当前状态 | 建议 |
|--------|---------|------|
| Mock支付接口 | ✅/❌ 无 @Profile 保护 | ⚠ 立即修复 - P0 |
| API Key 验证限流 | ❌ 无限制 | 增加 RateLimiter |
| 密码加密 | 使用 BCrypt/SCrypt | 确认使用强哈希算法 |
| SQL 注入 | ✅ JPA 基本免疫 | 检查 @Query 原生 SQL |
| XSS | ✅ 前端 Naive UI 自带转义 | 确认富文本区块的 XSS 过滤 |
| CORS | ⚠ 需确认 | Gateway 配置白名单而非通配符 |
| Token 传输 | ✅ Bearer Token | 确认是否仅 HTTPS 传输 |

### O3. CI/CD 检查点

```yaml
CI 流水线检查项:
  1. gradle test: 所有微服务单元测试通过
  2. vue-tsc: Admin 前端类型检查
  3. nuxt typecheck: Web 前端类型检查
  4. gradle build -x test: 编译通过
  5. docker build: 镜像构建成功

CD 流水线检查项:
  1. Flyway baseline + migrate: 数据库迁移
  2. readiness probe: 健康检查通过
  3. K8s rolling update: 滚动更新无中断

建议增加:
  - SpotBugs / PMD 静态代码分析
  - OWASP Dependency Check 依赖漏洞扫描
  - ArchUnit 架构守护测试
  - 集成测试(专设测试环境)
```

---

## 技能地图总览

```
┌─────────────────────────────────────────────────────────────────┐
│                    Project ZHANGYUAN Skills                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  业务技能         后端技能             测试技能        优化运维    │
│  ────────        ────────            ────────        ────────   │
│  B-001 SaaS闭环  DEV-001 DDD架构     T1 接口checklist O1 性能优化  │
│  B-002 双用户     DEV-002 Gateway路由  T2 微服务测试   O2 安全加固  │
│  B-003 CMS驱动   DEV-003 BFF模式      T3 缺陷5大模式  O3 CI/CD   │
│  B-004 双鉴权    DEV-004 Feign+Nacos                              │
│                  DEV-005 Flyway+JPA                               │
│                  DEV-006 Sa-Token RBAC                            │
│                  DB-001~004 数据库设计模式                          │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 复用建议

1. **新建 AI API 平台项目**：直接复用 B-001(计费闭环) + DEV-003(BFF) + DEV-006(RBAC) 模式
2. **新建 CMS 项目**：直接复用 B-003(CMS模式) + DB-003(版本链) + 8种区块类型
3. **新建 SaaS 项目**：直接复用 B-002(双用户)、DEV-001(DDD架构)、T1(测试checklist)
4. **学习/培训材料**：DEV-001(DDD示例) + T3(缺陷模式) 可作为团队技术培训素材
