import { request } from './http'
import type { BlockDefinition, PageBlock } from '../types/block'

export interface VersionInfo {
  versionId: number
  version: number
  publishedAt: string
  remark: string
}

export function getBlockDefinitions() {
  return request<BlockDefinition[]>('/admin/cms/block-definitions')
}

export function getDraftContent(pageId: number, locale: string) {
  return request<{ layout: string; blocks: PageBlock[] }>(
    `/admin/cms/pages/${pageId}/translations/${locale}/draft`
  )
}

export function getVersions(pageId: number, locale: string) {
  return request<VersionInfo[]>(
    `/admin/cms/pages/${pageId}/translations/${locale}/versions`
  )
}
