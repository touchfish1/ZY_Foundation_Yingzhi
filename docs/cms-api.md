# CMS 接口设计

## 响应格式

统一响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

分页响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [],
    "page": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

错误响应：

```json
{
  "code": 40001,
  "message": "invalid request",
  "data": null
}
```

## 前台 CMS 接口

### 获取已发布页面

```http
GET /api/cms/pages/render?path=/plans&locale=zh-CN
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "path": "/plans",
    "locale": "zh-CN",
    "title": "套餐价格",
    "seo": {
      "title": "套餐价格 - ZHANGYUAN",
      "description": "选择适合你的 API 套餐",
      "keywords": "API,套餐,价格"
    },
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
          "plans": []
        }
      }
    ]
  }
}
```

说明：

- 该接口只返回已发布版本。
- 服务端优先读取 Redis 缓存。
- 未发布或不存在页面返回 404 类型错误。

## 管理后台认证接口

### 登录

```http
POST /admin/auth/login
```

默认开发管理员：

```text
username: admin
password: admin123
```

生产环境必须通过环境变量覆盖默认密码和 `JWT_SECRET`。

请求：

```json
{
  "username": "admin",
  "password": "password"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "jwt-token",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员"
    },
    "permissions": [
      "cms:page:read",
      "cms:page:update",
      "cms:page:publish"
    ]
  }
}
```

### 当前用户

```http
GET /admin/auth/me
```

## 管理后台 CMS 接口

### 页面列表

```http
GET /admin/cms/pages?page=1&pageSize=20&keyword=plans
```

### 创建页面

```http
POST /admin/cms/pages
```

请求：

```json
{
  "slug": "/plans",
  "title": "套餐价格",
  "defaultLocale": "zh-CN"
}
```

### 页面详情

```http
GET /admin/cms/pages/{pageId}
```

### 保存草稿

```http
PUT /admin/cms/pages/{pageId}/translations/{locale}/draft
```

请求：

```json
{
  "title": "套餐价格",
  "seoTitle": "套餐价格 - ZHANGYUAN",
  "seoDescription": "选择适合你的 API 套餐",
  "seoKeywords": "API,套餐,价格",
  "content": {
    "layout": "default",
    "blocks": []
  }
}
```

### 发布页面

```http
POST /admin/cms/pages/{pageId}/translations/{locale}/publish
```

请求：

```json
{
  "remark": "发布套餐页第一版"
}
```

### 预览页面

```http
GET /admin/cms/pages/{pageId}/preview?locale=zh-CN&versionId=1
```

### 版本列表

```http
GET /admin/cms/pages/{pageId}/translations/{locale}/versions
```

### 回滚版本

```http
POST /admin/cms/pages/{pageId}/translations/{locale}/rollback
```

请求：

```json
{
  "versionId": 1,
  "remark": "回滚到稳定版本"
}
```

## 区块定义接口

### 区块定义列表

```http
GET /admin/cms/block-definitions
```

### 创建区块定义

```http
POST /admin/cms/block-definitions
```

### 更新区块定义

```http
PUT /admin/cms/block-definitions/{id}
```

## 媒体接口

### 上传文件

```http
POST /admin/assets/files
Content-Type: multipart/form-data
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "url": "https://cdn.example.com/assets/banner.png",
    "originalName": "banner.png",
    "contentType": "image/png",
    "sizeBytes": 123456
  }
}
```

## 套餐接口

### 管理后台套餐组

```http
GET    /admin/product/plan-groups
POST   /admin/product/plan-groups
PUT    /admin/product/plan-groups/{id}
```

### 管理后台套餐

```http
GET    /admin/product/plans
POST   /admin/product/plans
PUT    /admin/product/plans/{id}
```

### 前台套餐组

```http
GET /api/products/plan-groups/{code}
```

## 订单和支付接口

### 创建订单

```http
POST /api/orders
```

请求：

```json
{
  "planCode": "pro",
  "billingCycle": "monthly",
  "currency": "CNY"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "orderNo": "ZY202606030001",
    "amount": "29.00",
    "currency": "CNY",
    "status": "pending"
  }
}
```

### 创建支付

```http
POST /api/payments/checkout
```

请求：

```json
{
  "orderNo": "ZY202606030001",
  "channel": "mock"
}
```

### 支付回调

```http
POST /api/payments/callback/{channel}
```

第一版先实现 `mock` 渠道，跑通订单状态流转。
