import { request } from './http'
import type { BlockDefinition, PageBlock } from '../types/block'
import type { CmsPageDetail } from './cms'

export interface VersionInfo {
  versionId: number
  version: number
  publishedAt: string
  remark: string
}

// 获取所有可用区块类型的定义（字段 schema 等）
export function getBlockDefinitions() {
  console.log('[API] getBlockDefinitions')
  return request<BlockDefinition[]>('/admin/cms/block-definitions')
}

// 获取指定页面和语言版本的草稿内容（区块列表）
export function getDraftContent(pageId: number, locale: string) {
  console.log('[API] getDraftContent', { pageId, locale })
  return request<{ layout: string; blocks: PageBlock[] }>(
    `/admin/cms/pages/${pageId}/translations/${locale}/draft`
  )
}

// 获取指定页面和语言版本的历史版本列表
export function getVersions(pageId: number, locale: string) {
  console.log('[API] getVersions', { pageId, locale })
  return request<VersionInfo[]>(
    `/admin/cms/pages/${pageId}/translations/${locale}/versions`
  )
}

// 预览指定版本的页面内容
export function getPreviewContent(pageId: number, locale: string, versionId: number) {
  console.log('[API] getPreviewContent', { pageId, locale, versionId })
  return request<{ layout: string; blocks: PageBlock[] }>(
    `/admin/cms/pages/${pageId}/preview?locale=${encodeURIComponent(locale)}&versionId=${versionId}`
  )
}

// 回滚到指定历史版本（创建新版本覆盖当前草稿）
export function rollbackVersion(pageId: number, locale: string, versionId: number, remark: string) {
  console.log('[API] rollbackVersion', { pageId, locale, versionId, remark })
  return request<CmsPageDetail>(`/admin/cms/pages/${pageId}/translations/${locale}/rollback`, {
    method: 'POST',
    body: JSON.stringify({ versionId, remark })
  })
}
