# 实施路线

## MVP 范围

第一版交付目标：

- 管理员登录。
- RBAC 权限控制。
- 页面管理。
- 多语言页面内容。
- 区块式页面编辑。
- 页面预览。
- 发布和回滚。
- 媒体上传。
- 套餐管理。
- CMS Pricing 区块绑定真实套餐。
- 订单创建。
- Mock 支付。
- Docker Compose 一键启动。

第一版不做：

- 重型拖拽低代码。
- 审批流。
- 多租户。
- Elasticsearch。
- Kafka。
- A/B Test。
- 完整微服务拆分。

## 推荐目录结构

```text
Project_ZHANGYUAN/
  ZY_View_Migu/
    Admin/
    Web/
    Shared/
  ZY_Nexus_Congcong/
    Services/
      Api/
    Gateway/
  ZY_Source_Origin/
    Domain/
    SharedKernel/
  ZY_Archive_Shirou/
    Migrations/
    Seeds/
    Backup/
  ZY_Guard_Bo/
    Auth/
    Rbac/
    Crypto/
  ZY_Foundation_Yingzhi/
    Docker/
    Kubernetes/
    Helm/
    Ci/
  docs/
```

## 后端目录结构

```text
ZY_Nexus_Congcong/Services/Api/src/main/java/com/zhangyuan/
  ZhangyuanApplication.java
  modules/
    auth/
    cms/
    asset/
    product/
    order/
    payment/
    system/
  common/
    config/
    exception/
    response/
    security/
    util/
```

## 管理后台页面

```text
/admin/login
/admin/dashboard
/admin/cms/pages
/admin/cms/pages/create
/admin/cms/pages/:id/edit
/admin/cms/pages/:id/versions
/admin/assets
/admin/products/plan-groups
/admin/products/plans
/admin/orders
/admin/payments/transactions
/admin/system/users
/admin/system/roles
/admin/system/audit-logs
/admin/system/settings
```

## 页面编辑器

布局：

```text
顶部栏：返回、语言切换、保存草稿、预览、发布
左侧：区块列表，支持添加、复制、删除、上下移动
中间：当前区块配置表单
右侧：页面预览
```

第一版不做自由拖拽画布，只做区块编排和 schema 表单配置。

## 前台组件

```text
components/blocks/HeroBlock.vue
components/blocks/PricingBlock.vue
components/blocks/FeatureGridBlock.vue
components/blocks/FaqBlock.vue
components/blocks/CtaBlock.vue
components/blocks/RichTextBlock.vue
components/blocks/ImageBannerBlock.vue
components/blocks/StatsBlock.vue
```

## 阶段计划

### 阶段 1：基础设施

目标：系统能启动，有数据库，有登录，有权限基础。

任务：

- 初始化 monorepo。
- 初始化 Spring Boot API。
- 接入 PostgreSQL。
- 接入数据库 migration 工具。
- 接入 Redis。
- 实现统一响应结构。
- 实现统一异常处理。
- 实现管理员登录。
- 实现 JWT 鉴权。
- 实现 RBAC 权限模型。
- 编写 Docker Compose。

当前状态：Spring Boot API、PostgreSQL migration、Redis 配置、统一响应、管理员登录、JWT、RBAC 初始权限、Docker Compose 基础依赖已完成。

### 阶段 2：CMS 核心

目标：运营可以新增页面、编辑多语言内容、发布页面。

任务：

- 页面 CRUD。
- 页面多语言 Translation。
- 页面版本 Version。
- 草稿保存。
- 页面预览。
- 页面发布。
- 页面回滚。
- 区块定义管理。
- 页面发布缓存。
- 媒体上传。
- Nuxt 动态路由渲染。

当前状态：后端页面创建、列表、详情、草稿、版本、发布、前台渲染和默认区块定义已完成。管理后台 UI、媒体上传、Nuxt 动态渲染待实现。

### 阶段 3：套餐和订单

目标：CMS 页面可以绑定真实套餐，并跑通购买链路。

任务：

- 套餐组管理。
- 套餐管理。
- 价格管理。
- 权益管理。
- Pricing 区块绑定套餐组。
- 创建订单。
- 订单快照。
- Mock 支付。
- 支付回调模拟。
- 订单状态更新。

### 阶段 4：上线能力

目标：具备基础生产部署和后续 K8s 演进能力。

任务：

- 审计日志。
- 操作日志。
- 健康检查。
- 构建 Docker 镜像。
- 编写 Nginx 配置。
- 编写 K8s manifests 或 Helm Chart。
- 编写 CI/CD 流水线。
- 添加基础监控指标。

## 第一版验收标准

运营侧：

- 可以登录后台。
- 可以创建 `/plans` 页面。
- 可以创建 `zh-CN` 和 `en-US` 内容。
- 可以添加 Hero、Pricing、FAQ 区块。
- 可以绑定真实套餐组。
- 可以预览页面。
- 可以发布页面。
- 可以回滚历史版本。

用户侧：

- 可以访问 `/plans`。
- 可以访问 `/en/plans`。
- 可以看到真实套餐价格。
- 可以点击购买。
- 可以创建订单。
- 可以完成 Mock 支付。
- 支付成功后订单状态变为 `paid`。
