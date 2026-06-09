import { request } from './http'
import type { PageResponse } from '../types/common'

export interface OrderItem {
  orderNo: string
  amount: string
  currency: string
  status: string
  createdAt: string
}

export interface OrderDetail {
  orderNo: string
  planId: number
  priceId: number
  amount: string
  currency: string
  status: string
  snapshotJson?: string
  createdAt: string
  paidAt: string | null
  userId?: number
  // populated client-side from snapshotJson
  planName?: string
  planCode?: string
  billingCycle?: string
}

export interface SubscriptionItem {
  id: number
  userId: number
  planCode: string
  planName: string
  status: string
  startsAt: string
  expiresAt: string
  createdAt: string
  active: boolean
}

export interface UsageRecord {
  id: number
  userId: number
  model: string
  callCount: number
  quotaUsed: number
  date: string
}

export interface UsageSummary {
  totalQuota: number
  dailyAverage: number
  totalCalls: number
  recordCount: number
}

export function listOrders(page = 1, pageSize = 20) {
  return request<PageResponse<OrderItem>>(`/admin/orders?page=${page}&pageSize=${pageSize}`)
}

export function getOrder(orderNo: string) {
  return request<OrderDetail>(`/admin/orders/${orderNo}`)
}

export function listSubscriptions() {
  return request<SubscriptionItem[]>('/admin/subscriptions')
}

export function getUsageRecords(userId: number, params?: { page?: number, pageSize?: number, startDate?: string, endDate?: string }) {
  const qs = new URLSearchParams()
  if (params?.page) qs.set('page', String(params.page))
  if (params?.pageSize) qs.set('pageSize', String(params.pageSize))
  if (params?.startDate) qs.set('startDate', params.startDate)
  if (params?.endDate) qs.set('endDate', params.endDate)
  const query = qs.toString()
  return request<PageResponse<UsageRecord>>(`/admin/usage/${userId}${query ? '?' + query : ''}`)
}

export function getUsageSummary(userId: number) {
  return request<UsageSummary>(`/admin/usage/${userId}/summary`)
}
