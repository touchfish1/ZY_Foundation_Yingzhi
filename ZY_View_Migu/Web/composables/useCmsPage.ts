import type { ApiResponse, CmsPage } from '~/types/cms'

// 根据路径和语言环境获取 CMS 页面快照数据
export async function useCmsPage(path: string, locale = 'zh-CN') {
  const config = useRuntimeConfig()
  const url = `${config.public.apiBase}/api/cms/pages/render?path=${encodeURIComponent(path)}&locale=${encodeURIComponent(locale)}`
  console.log(`[useCmsPage] GET ${url}`)
  const { data, error } = await useFetch<ApiResponse<CmsPage>>(url)

  if (error.value || data.value?.code !== 0 || !data.value?.data) {
    console.warn(`[useCmsPage] Failed: path=${path}, error=${error.value?.message || 'data is null'}`)
    return null
  }

  console.log(`[useCmsPage] Success: path=${path}`)
  return data.value.data
}
