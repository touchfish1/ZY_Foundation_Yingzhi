# AGENTS.md — Project ZHANGYUAN

## 项目速览

多模块单体（CMS 子系统），命名体系 `ZY_<Function>_<MythName>`。

| 目录 | 技术栈 |
|---|---|
| `ZY_View_Migu/Web` | Nuxt 3 + TypeScript |
| `ZY_View_Migu/Admin` | Vue 3 + Vite + Naive UI + Pinia |
| `ZY_Nexus_Congcong/Services/Api` | Spring Boot 3.3.5 + Java 21 + Gradle |
| `ZY_Archive_Shirou/Migrations` | Flyway (PostgreSQL) |
| `ZY_Guard_Bo/` | 认证/授权（目前实现在 API 内） |
| `ZY_Foundation_Yingzhi/Docker/` | Docker Compose（PostgreSQL + Redis + MinIO + Nginx） |

**无根目录 package.json**，Web 和 Admin 各自独立 `npm install` / `npm run dev`。

## 开发命令

```bash
# 启动中间件（PostgreSQL + Redis + MinIO）
docker compose -f ZY_Foundation_Yingzhi/Docker/docker-compose.yml up -d

# 启动全栈（中间件 + API + Web + Admin + Nginx）
docker compose -f ZY_Foundation_Yingzhi/Docker/docker-compose.yml -f ZY_Foundation_Yingzhi/Docker/docker-compose.app.yml up -d

# Web 前台（:3000）
cd ZY_View_Migu/Web && npm install && npm run dev

# Admin 后台（:5173）
cd ZY_View_Migu/Admin && npm install && npm run dev

# API 后端（:8080）
cd ZY_Nexus_Congcong/Services/Api && gradle bootRun

# 仅运行测试
cd ZY_Nexus_Congcong/Services/Api && gradle test
```

**Docker Compose 文件拆分**：
- `docker-compose.yml` — 中间件（PostgreSQL / Redis / MinIO），开发时只需启动此文件
- `docker-compose.app.yml` — 应用服务（API / Web / Admin / Nginx），需与中间件文件叠加使用

**Admin Vite 代理**：`/admin` `/api` `/actuator` → `localhost:8080`

**Web 后端地址**：默认 `http://localhost:8080`，通过 `NUXT_PUBLIC_API_BASE` 覆盖。

## WSL 开发模式

Docker Compose 在 WSL 中运行，API 在 Windows 侧启动时：

```bash
$env:DB_HOST='100.125.148.23'; $env:REDIS_HOST='100.125.148.23'; $env:MINIO_ENDPOINT='http://100.125.148.23:9000'; gradle bootRun
```

或使用 `application-wsl.yml` profile（如果存在）。

## 后端关键约定

- **Flyway 管理 schema**，JPA `ddl-auto: validate`（不自动建表）。
- Migration 路径：`src/main/resources/db/migration/V*.sql`
- **统一响应格式**：`{ code: 0, message: "ok", data: ... }`，分页额外返回 `items/page/pageSize/total`。
- 默认管理员：`admin / admin123`（生产通过 `DEFAULT_ADMIN_PASSWORD` 覆盖）。
- JWT 密钥：`JWT_SECRET` 环境变量（开发有硬编码默认值，生产必须换）。
- `gradlew` 不存在，依赖系统安装的 Gradle。

## DDD 架构（模块化单体）

后端已从 CRUD 贫血模型逐步迁移为六边形架构（Hexagonal Architecture）的 DDD 模块化单体。

### 包结构（每个模块）

```
com.zhangyuan.modules.{module}/
├── domain/
│   ├── model/          # 聚合根 + 实体 + 值对象（零框架依赖）
│   ├── repository/     # 仓库接口（端口）
│   ├── service/        # 领域服务
│   └── event/          # 领域事件
├── application/
│   └── service/        # 应用服务（事务编排、DTO 映射）
├── adapter/
│   ├── in/rest/        # REST 控制器（入站适配器）
│   └── out/persistence/# JPA 实现（出站适配器）
```

### 跨模块通信

- 模块间同步调用通过注入 ApplicationService
- 异步解耦通过领域事件（`DomainEventPublisher` + `@TransactionalEventListener`）
- 共享内核在 `common/ddd/`：`Entity`、`AggregateRoot`、`ValueObject`、`DomainEvent`、`Money`

### 迁移状态

所有 7 个模块（auth / cms / product / order / payment / asset / system）已完成 DDD 骨架创建：
- Rich domain model 含业务行为方法（`Order.markPaid()`，`Payment.markSuccess()`）
- 状态机显式表达（`OrderStatus.canTransitionTo()`）
- Repository 端口在 domain 层定义，adapter 层实现
- 新路径 `/api/ddd/*` 用于隔离验证，与旧路径并行运行

### DDD 验证命令

```bash
# 运行模块边界验证
cd ZY_Nexus_Congcong/Services/Api && gradle test --tests "com.zhangyuan.architecture.*"

# 运行全部测试
cd ZY_Nexus_Congcong/Services/Api && gradle test
```

### 关键约定

- **Domain 层禁止依赖 Spring/JPA**，仅使用 `common/ddd/` 和 JDK
- **Adapter 层** 负责领域对象 ↔ JPA 实体映射
- **Application 层** 只编排不包含业务规则
- 新功能优先在 DDD 结构下实现，旧 CRUD 代码逐步淘汰

## 模块边界

- 新功能代码落入现有 6 个模块，**不新增顶层目录**。
- 领域逻辑暂在 API 内按模块组织（`modules/auth`, `modules/cms` 等），稳定后再下沉到 `ZY_Source_Origin`。
- CMS 不承载套餐/订单/支付的核心规则。
- `ZY_Guard_Bo` 和 `ZY_Nexus_Congcong/Gateway` 目前仅占位，实现仍在 API 单体中。

## 前台渲染

Nuxt 动态路由 `pages/[...slug].vue` 通过 `GET /api/cms/pages/render?path=...&locale=...` 获取已发布快照，按 `block.type` 映射到 `components/blocks/` 下对应组件。未知区块忽略不崩溃。
