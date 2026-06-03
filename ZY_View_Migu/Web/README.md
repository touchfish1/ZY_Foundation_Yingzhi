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
