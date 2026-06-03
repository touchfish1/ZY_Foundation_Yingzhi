export interface CmsBlock {
  id: string
  type: string
  props: Record<string, unknown>
}

export interface CmsPage {
  path: string
  locale: string
  title: string
  seo: {
    title: string
    description: string
    keywords: string
  }
  layout: string
  blocks: CmsBlock[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
