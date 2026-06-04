export interface PageResponse<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}
