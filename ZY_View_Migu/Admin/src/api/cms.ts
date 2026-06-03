import { request } from './http'

export interface CmsPageListItem {
  id: number
  slug: string
  defaultLocale: string
  status: string
  updatedAt: string
}

export interface CmsPageDetail {
  id: number
  slug: string
  defaultLocale: string
  status: string
  translations: Array<{
    locale: string
    title: string
    seoTitle?: string
    seoDescription?: string
    seoKeywords?: string
    draftVersionId?: number
    publishedVersionId?: number
    status: string
  }>
}

export function listPages() {
  return request<CmsPageListItem[]>('/admin/cms/pages')
}

export function createPage(payload: { slug: string; title: string; defaultLocale: string }) {
  return request<CmsPageDetail>('/admin/cms/pages', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function getPage(id: number) {
  return request<CmsPageDetail>(`/admin/cms/pages/${id}`)
}

export function saveDraft(id: number, locale: string, payload: unknown) {
  return request<CmsPageDetail>(`/admin/cms/pages/${id}/translations/${locale}/draft`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function publishPage(id: number, locale: string, remark: string) {
  return request<CmsPageDetail>(`/admin/cms/pages/${id}/translations/${locale}/publish`, {
    method: 'POST',
    body: JSON.stringify({ remark })
  })
}
