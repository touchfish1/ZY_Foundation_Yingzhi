# CMS 架构方案

## 目标

Project ZHANGYUAN 需要一个可落地、美观、低内存占用、可扩展的 CMS 子系统，用于管理前端页面。CMS 后续需要接入套餐、订单、支付、用户权限等系统，并预留 Docker、Kubernetes 和 CI/CD 演进空间。

CMS 不做成重型低代码平台，也不做成只能维护固定页面的配置后台。推荐采用轻量区块式页面 CMS，让运营可以自由新增页面、配置多语言内容、组合页面区块，并将业务区块绑定真实业务数据。

## 系统定位

CMS 负责：

- 页面管理，包括新增、编辑、删除、启用、禁用。
- 多语言页面内容管理。
- 页面区块管理，包括 Hero、Pricing、FAQ、CTA、Rich Text 等。
- 草稿、预览、发布、回滚。
- SEO 信息配置。
- 媒体资源引用。
- 与套餐、订单、支付系统联动。

CMS 不负责：

- 真实支付渠道的底层结算逻辑。
- 订单生命周期的完整业务规则。
- 用户订阅权益发放。
- 重型拖拽建站能力。
- A/B Test、审批流、复杂多租户。

## 推荐技术栈

```text
前台站点：Nuxt 3 + TypeScript
管理后台：Vue 3 + TypeScript + Vite + Naive UI
后端 API：Spring Boot 3 + Java 21
数据库：PostgreSQL
缓存：Redis
对象存储：MinIO
部署：Docker Compose 起步，后续 Kubernetes
CI/CD：GitHub Actions 或 GitLab CI
```

## 架构形态

第一阶段采用模块化单体：

```text
ZY_View_Migu          前台站点和管理后台
ZY_Nexus_Congcong     后端 API、网关、服务适配
ZY_Source_Origin      领域模型、领域服务、公共能力
ZY_Archive_Shirou     数据库迁移、存储、备份
ZY_Guard_Bo           认证、授权、安全策略
ZY_Foundation_Yingzhi DevOps、Docker、Kubernetes、CI/CD
docs                  项目文档
```

后端模块边界：

```text
auth        登录、JWT、RBAC、管理员账号
cms         页面、区块、模板、多语言、版本、发布
asset       图片、附件、MinIO 对接
product     套餐、套餐组、价格、权益
order       订单、订单快照、订单状态
payment     支付渠道、支付单、支付回调
system      配置、审计日志、操作日志、字典
integration 对外接口、Webhook、事件
```

后续可拆分为：

```text
api-gateway
auth-service
cms-service
product-service
order-service
payment-service
asset-service
```

## 页面模型

CMS 页面由三层组成：

```text
Page        页面，例如 /plans
Translation 页面语言版本，例如 zh-CN、en-US
Block       页面区块，例如 hero、pricing、faq
```

页面内容示例：

```json
{
  "layout": "default",
  "blocks": [
    {
      "id": "hero_001",
      "type": "hero",
      "props": {
        "title": "选择适合你的套餐",
        "subtitle": "稳定、高性能的 API 服务"
      }
    },
    {
      "id": "pricing_001",
      "type": "pricing",
      "props": {
        "planGroupCode": "api_plans",
        "defaultBillingCycle": "monthly",
        "highlightPlanCode": "pro"
      }
    }
  ]
}
```

Pricing 区块只保存套餐组编码，不直接保存价格。发布时后端读取真实套餐、价格和权益，生成稳定快照。

## 区块类型

第一版内置区块：

- `hero`：页面头图和主标题。
- `pricing`：套餐价格区块，绑定真实套餐组。
- `feature-grid`：功能列表。
- `faq`：常见问题。
- `cta`：行动按钮。
- `rich-text`：富文本内容。
- `image-banner`：图片横幅。
- `stats`：数据展示。

每个区块由 schema 驱动后台表单。

```json
{
  "type": "hero",
  "name": "Hero 区块",
  "fields": [
    {
      "key": "title",
      "label": "标题",
      "type": "text",
      "required": true
    },
    {
      "key": "subtitle",
      "label": "副标题",
      "type": "textarea"
    }
  ]
}
```

## 多语言策略

一个页面 slug 下可以有多个语言版本。每个语言版本有独立草稿、独立发布版本、独立 SEO。

推荐路由：

```text
/plans       中文默认页面
/en/plans    英文页面
```

后台上体现为：

```text
/plans
  zh-CN 已发布
  en-US 草稿
```

## 发布机制

发布流程：

```text
保存草稿
校验 content_json
解析区块依赖
读取业务数据
生成 snapshot_json
写入版本表
更新 published_version_id
刷新 Redis 缓存
记录发布日志
```

前台只读取已发布快照，避免运营编辑中的草稿影响线上页面。

## 前台渲染

Nuxt 使用动态路由读取 CMS：

```text
GET /api/cms/pages/render?path=/plans&locale=zh-CN
```

前台根据返回的 blocks 做组件映射：

```text
hero -> HeroBlock
pricing -> PricingBlock
faq -> FaqBlock
cta -> CtaBlock
```

未知区块应忽略并上报日志，避免整页崩溃。

## 低内存策略

- 第一阶段使用模块化单体，减少服务数量。
- 不引入 Elasticsearch、Kafka、重型低代码引擎。
- Redis 缓存已发布页面快照。
- 管理后台和前台构建为静态资源，由 Nginx 托管。
- 图片、附件存放 MinIO，不进入数据库。
- Spring Boot 生产环境限制 JVM，例如 `-Xms256m -Xmx768m`。
