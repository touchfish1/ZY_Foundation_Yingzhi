import type { ApiResponse, CmsPage } from '~/types/cms'

export async function useCmsPage(path: string, locale = 'zh-CN') {
  const config = useRuntimeConfig()
  const url = `${config.public.apiBase}/api/cms/pages/render?path=${encodeURIComponent(path)}&locale=${encodeURIComponent(locale)}`
  const { data, error } = await useFetch<ApiResponse<CmsPage>>(url)

  if (error.value || data.value?.code !== 0 || !data.value?.data) {
    throw createError({ statusCode: 404, statusMessage: 'Page not found' })
  }

  return data.value.data
}
