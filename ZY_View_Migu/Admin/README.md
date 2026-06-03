# ZY_View_Migu/Admin

Origin: ZHANGYUAN - Module: MIGU (Admin Console)

该目录用于承载 CMS 管理后台。

## Target Stack

- Vue 3
- TypeScript
- Vite
- Naive UI
- Pinia
- Vue Router

## Planned Structure

```text
src/
  api/
  components/
    page-editor/
  layouts/
  pages/
    login/
    dashboard/
    cms/
    assets/
    products/
    orders/
    system/
  router/
  store/
  styles/
  types/
```

## Local Run

```bash
npm install
npm run dev
```

默认访问：

```text
http://localhost:5173
```

开发服务器会将 `/admin`、`/api`、`/actuator` 代理到：

```text
http://localhost:8080
```

默认登录账号：

```text
admin / admin123
```
