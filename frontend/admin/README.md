# frontend/admin — 管理后台

Vue 3 + TypeScript + Vite + Naive UI + Pinia 管理后台。

## 开发

```bash
npm install
npm run dev
```

默认 `http://localhost:5173`，Vite 代理 `/admin`、`/api`、`/actuator` 到 `http://localhost:8080`。

## 页面模块

- `login` — 登录
- `dashboard` — 仪表盘
- `cms` — 页面管理、区块定义
- `assets` — 素材管理
- `products` — 套餐与定价
- `orders` — 订单管理
- `payments` — 支付管理
- `system` — 系统设置、用户管理、角色管理
