export interface SiteSettings {
  siteName: string
  siteDescription: string
  icpFiling: string
  footerText: string
}

// 获取网站全局设置（站点名称、描述、ICP 备案、底部文本）
export async function useSiteSettings() {
  const config = useRuntimeConfig()
  const url = `${config.public.apiBase}/api/system/settings`
  console.log(`[useSiteSettings] GET ${url}`)
  const { data, error } = await useFetch<SiteSettings>(url)

  if (error.value) {
    console.error('[useSiteSettings] Failed to load', error.value)
    return { siteName: '', siteDescription: '', icpFiling: '', footerText: '' } as SiteSettings
  }

  console.log(`[useSiteSettings] Success: siteName=${data.value?.siteName}`)
  return data.value || ({} as SiteSettings)
}
