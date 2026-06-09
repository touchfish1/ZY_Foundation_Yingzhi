import { request } from './http'
import type { PageResponse } from '../types/common'

export interface CmsPageListItem {
  id: number
  slug: string
  defaultLocale: string
  status: string
  pageType: string
  createdAt: string
  updatedAt: string
}

export interface CmsPageDetail {
  id: number
  slug: string
  defaultLocale: string
  status: string
  pageType: string
  translations: Array<{
    locale: string
    title: string
    seoTitle?: string
    seoDescription?: string
    seoKeywords?: string
    draftVersionId?: number
    publishedVersionId?: number
    content?: unknown
    status: string
  }>
}

// 获取 CMS 页面列表（分页，支持关键词搜索）
export function listPages(page = 1, pageSize = 20, keyword?: string) {
  console.log('[API] listPages', { page, pageSize, keyword })
  const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
  if (keyword) params.set('keyword', keyword)
  return request<PageResponse<CmsPageListItem>>(`/admin/cms/pages?${params.toString()}`)
}

// 创建新页面：指定路径（slug）、标题、默认语言和页面类型
export function createPage(payload: { slug: string; title: string; defaultLocale: string; pageType?: string }) {
  console.log('[API] createPage', payload)
  return request<CmsPageDetail>('/admin/cms/pages', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

// 根据 ID 获取页面详情（含所有语言翻译信息）
export function getPage(id: number) {
  console.log('[API] getPage', { id })
  return request<CmsPageDetail>(`/admin/cms/pages/${id}`)
}

// 保存指定语言版本的草稿内容
export function saveDraft(id: number, locale: string, payload: unknown) {
  console.log('[API] saveDraft', { id, locale })
  return request<CmsPageDetail>(`/admin/cms/pages/${id}/translations/${locale}/draft`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

// 发布指定语言版本的页面（创建新发布版本）
export function publishPage(id: number, locale: string, remark: string) {
  console.log('[API] publishPage', { id, locale, remark })
  return request<CmsPageDetail>(`/admin/cms/pages/${id}/translations/${locale}/publish`, {
    method: 'POST',
    body: JSON.stringify({ remark })
  })
}

// 更新页面基本信息（路径、默认语言）
export function updatePage(id: number, payload: { slug?: string; defaultLocale?: string; status?: string }) {
  console.log('[API] updatePage', { id, ...payload })
  return request<CmsPageDetail>(`/admin/cms/pages/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

// 删除页面及其所有翻译版本
export function deletePage(id: number) {
  console.log('[API] deletePage', { id })
  return request<void>(`/admin/cms/pages/${id}`, {
    method: 'DELETE'
  })
}
