export interface SiteSettings {
  siteName: string
  siteDescription: string
  icpFiling: string
  footerText: string
}

function normalizeSettings(raw: Record<string, string>): SiteSettings {
  return {
    siteName: raw.site_name || raw.siteName || '',
    siteDescription: raw.site_description || raw.siteDescription || '',
    icpFiling: raw.icp_filing || raw.icpFiling || '',
    footerText: raw.footer_text || raw.footerText || ''
  }
}

export async function useSiteSettings() {
  const config = useRuntimeConfig()
  const url = `${config.public.apiBase}/api/system/settings`
  const { data, error } = await useFetch<Record<string, string>>(url)

  if (error.value) {
    console.error('[useSiteSettings] Failed to load', error.value)
    return normalizeSettings({})
  }

  return normalizeSettings(data.value || {})
}
