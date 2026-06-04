# 工程结构落地方案

所有代码应落入以下模块边界中。

## 顶层模块职责

| 目录 | 职责 | CMS 相关内容 |
| --- | --- | --- |
| `frontend` | 前端页面、UI、交互 | Nuxt 前台、Vue 管理后台、共享 UI 类型 |
| `backend` | 后端服务、网络层、接口 | Spring Boot API、Admin API、Public API、网关适配 |
| `domain` | 核心源码、领域逻辑、公共能力 | CMS 领域模型、发布逻辑、套餐领域规则、订单领域规则 |
| `database` | 数据库、迁移、备份、存储 | PostgreSQL migration、seed、备份脚本 |
| `security` | 认证、授权、安全 | JWT、RBAC、权限码、安全策略 |
| `infrastructure` | 基础设施、DevOps | Docker Compose、K8s、Helm、CI/CD |
| `docs` | 文档 | 架构、数据库、接口、部署、路线图 |

## 推荐目录结构

```text
Project_ZHANGYUAN/
  frontend/
    admin/
      package.json
      src/
    web/
      package.json
      pages/
      components/
    shared/
      types/
      api-client/

  backend/
    api/
      build.gradle
      src/main/java/com/zhangyuan/
    gateway/

  domain/
    Cms/
    Product/
    Order/
    Payment/
    SharedKernel/

  database/
    migrations/
      V001__init_auth.sql
      V002__init_cms.sql
      V003__init_product.sql
      V004__init_order_payment.sql
    seeds/
    backup/

  security/
    auth/
    rbac/
    crypto/

  infrastructure/
    docker/
      docker-compose.yml
      nginx/
    kubernetes/
    helm/
    ci/

  docs/
```

## 前端落点

### 管理后台

管理后台放在：

```text
frontend/admin
```

技术栈：

```text
Vue 3
TypeScript
Vite
Naive UI
Pinia
Vue Router
```

主要模块：

```text
src/pages/login
src/pages/dashboard
src/pages/cms
src/pages/assets
src/pages/products
src/pages/orders
src/pages/system
src/components/page-editor
src/api
src/router
src/store
```

### 前台站点

前台站点放在：

```text
frontend/web
```

技术栈：

```text
Nuxt 3
TypeScript
```

主要模块：

```text
pages/[...slug].vue
components/blocks/HeroBlock.vue
components/blocks/PricingBlock.vue
components/blocks/FaqBlock.vue
components/blocks/CtaBlock.vue
composables/useCmsPage.ts
```

## 后端落点

Spring Boot API 放在：

```text
backend/api
```

推荐 Java 包结构：

```text
com.zhangyuan
  ZhangyuanApplication
  common
    config
    exception
    response
    security
  modules
    auth
    cms
    asset
    product
    order
    payment
    system
```

第一阶段为了降低复杂度，领域逻辑可以先在 API 工程内按模块组织。等业务稳定后，再将通用领域模型下沉到：

```text
domain/
```

## 数据库落点

数据库 migration 放在：

```text
database/migrations
```

建议使用 Flyway。版本规划：

```text
V001__init_auth.sql
V002__init_cms.sql
V003__init_asset.sql
V004__init_product.sql
V005__init_order_payment.sql
V006__init_audit_log.sql
```

Seed 数据放在：

```text
database/seeds
```

包括：

- 默认管理员。
- 默认角色。
- 默认权限码。
- 默认 CMS 区块定义。
- 示例套餐组和套餐。

## 安全落点

安全相关设计和可复用实现放在：

```text
security
```

第一阶段 API 工程内可以直接实现 JWT 和 RBAC，避免过早拆包。稳定后再沉淀到 `security`。

权限码示例：

```text
cms:page:read
cms:page:create
cms:page:update
cms:page:publish
cms:page:delete
product:plan:update
order:read
payment:transaction:read
system:user:manage
system:role:manage
```

## 基础设施落点

Docker、Kubernetes、CI/CD 放在：

```text
infrastructure
```

推荐结构：

```text
infrastructure/
  docker/
    docker-compose.yml
    nginx/
      nginx.conf
  kubernetes/
    api-deployment.yaml
    web-deployment.yaml
    admin-deployment.yaml
    ingress.yaml
  helm/
    zhangyuan/
  ci/
    github-actions.yml
```

## 实施原则

- 尊重现有模块命名体系，不新增割裂的顶层结构。
- 第一阶段优先模块化单体，减少部署复杂度和内存占用。
- 前端、后端、数据库、基础设施分别落在已有模块中。
- 后续拆分服务时，按业务边界拆，不按技术层随意拆。
- CMS 是子系统，不应把套餐、订单、支付的核心规则塞入 CMS。
