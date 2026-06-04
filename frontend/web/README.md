# frontend/web — 前台站点

Nuxt 3 前台站点，包含独立手写首页和 CMS 动态页渲染。

## 开发

```bash
npm install
npm run dev
```

默认 `http://localhost:3000`，通过 `NUXT_PUBLIC_API_BASE` 连接后端（默认 `http://localhost:8080`）。

## 结构

- `pages/index.vue` — 独立手写首页（不走 CMS）
- `pages/[...slug].vue` — CMS 动态页渲染
- `components/blocks/` — CMS 区块组件（hero、pricing、feature-grid、faq、cta、rich-text）
- `composables/` — `useCmsPage`、`useSiteSettings` 等
- 动画组件：`AnimReveal`、`AnimParticles`、`AnimWaves`、`AnimTrail` 等
