// 设置页面 SEO 元信息（标题、描述、关键词）
export function usePageMeta(title: string, description: string, keywords?: string) {
  console.log(`[usePageMeta] title=${title}, description=${description}, keywords=${keywords}`)
  useHead({
    title,
    meta: [
      { name: 'description', content: description },
      ...(keywords ? [{ name: 'keywords', content: keywords }] : [])
    ]
  })
}
