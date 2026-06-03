# 工程结构落地方案

Project ZHANGYUAN 已经采用 `ZY_<Function>_<MythName>` 命名体系，因此 CMS 子系统不再额外引入 `apps/`、`services/` 这类平行目录。所有新代码应落入现有模块边界中。

## 顶层模块职责

| 目录 | 职责 | CMS 相关内容 |
| --- | --- | --- |
| `ZY_View_Migu` | 前端页面、UI、交互 | Nuxt 前台、Vue 管理后台、共享 UI 类型 |
| `ZY_Nexus_Congcong` | 后端服务、网络层、接口 | Spring Boot API、Admin API、Public API、网关适配 |
| `ZY_Source_Origin` | 核心源码、领域逻辑、公共能力 | CMS 领域模型、发布逻辑、套餐领域规则、订单领域规则 |
| `ZY_Archive_Shirou` | 数据库、迁移、备份、存储 | PostgreSQL migration、seed、备份脚本 |
| `ZY_Guard_Bo` | 认证、授权、安全 | JWT、RBAC、权限码、安全策略 |
| `ZY_Foundation_Yingzhi` | 基础设施、DevOps | Docker Compose、K8s、Helm、CI/CD |
| `docs` | 文档 | 架构、数据库、接口、部署、路线图 |

## 推荐目录结构

```text
Project_ZHANGYUAN/
  ZY_View_Migu/
    Admin/
      package.json
      src/
    Web/
      package.json
      pages/
      components/
    Shared/
      types/
      api-client/

  ZY_Nexus_Congcong/
    Services/
      Api/
        build.gradle
        src/main/java/com/zhangyuan/
    Gateway/

  ZY_Source_Origin/
    Domain/
      Cms/
      Product/
      Order/
      Payment/
    SharedKernel/

  ZY_Archive_Shirou/
    Migrations/
      V001__init_auth.sql
      V002__init_cms.sql
      V003__init_product.sql
      V004__init_order_payment.sql
    Seeds/
    Backup/

  ZY_Guard_Bo/
    Auth/
    Rbac/
    Crypto/

  ZY_Foundation_Yingzhi/
    Docker/
      docker-compose.yml
      nginx/
    Kubernetes/
    Helm/
    Ci/

  docs/
```

## 前端落点

### 管理后台

管理后台放在：

```text
ZY_View_Migu/Admin
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
ZY_View_Migu/Web
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
ZY_Nexus_Congcong/Services/Api
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
ZY_Source_Origin/Domain
```

## 数据库落点

数据库 migration 放在：

```text
ZY_Archive_Shirou/Migrations
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
ZY_Archive_Shirou/Seeds
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
ZY_Guard_Bo
```

第一阶段 API 工程内可以直接实现 JWT 和 RBAC，避免过早拆包。稳定后再沉淀到 `ZY_Guard_Bo`。

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
ZY_Foundation_Yingzhi
```

推荐结构：

```text
ZY_Foundation_Yingzhi/
  Docker/
    docker-compose.yml
    nginx/
      nginx.conf
  Kubernetes/
    api-deployment.yaml
    web-deployment.yaml
    admin-deployment.yaml
    ingress.yaml
  Helm/
    zhangyuan/
  Ci/
    github-actions.yml
```

## 实施原则

- 尊重现有模块命名体系，不新增割裂的顶层结构。
- 第一阶段优先模块化单体，减少部署复杂度和内存占用。
- 前端、后端、数据库、基础设施分别落在已有模块中。
- 后续拆分服务时，按业务边界拆，不按技术层随意拆。
- CMS 是子系统，不应把套餐、订单、支付的核心规则塞入 CMS。
