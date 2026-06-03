export interface SiteSettings {
  siteName: string
  siteDescription: string
  icpFiling: string
  footerText: string
}

export async function useSiteSettings() {
  const config = useRuntimeConfig()
  const url = `${config.public.apiBase}/api/system/settings`
  const { data, error } = await useFetch<SiteSettings>(url)

  if (error.value) {
    console.error('Failed to load site settings', error.value)
    return { siteName: '', siteDescription: '', icpFiling: '', footerText: '' } as SiteSettings
  }

  return data.value || ({} as SiteSettings)
}
