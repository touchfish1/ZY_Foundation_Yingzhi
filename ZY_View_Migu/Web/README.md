# ZY_View_Migu/Web

Origin: ZHANGYUAN - Module: MIGU (Public Web)

该目录用于承载 Nuxt 3 前台站点。

## Target Stack

- Nuxt 3
- TypeScript

## Planned Structure

```text
pages/
  [...slug].vue
components/
  blocks/
composables/
types/
```

前台页面通过 CMS 渲染接口读取已发布页面快照，并根据区块类型映射到 Vue 组件。

## Local Run

```bash
npm install
npm run dev
```

默认访问：

```text
http://localhost:3000/plans
```

默认后端地址：

```text
http://localhost:8080
```

可以通过环境变量覆盖：

```bash
NUXT_PUBLIC_API_BASE=http://localhost:8080 npm run dev
```

已实现区块：

- `hero`
- `pricing`
- `feature-grid`
- `faq`
- `cta`
- `rich-text`
