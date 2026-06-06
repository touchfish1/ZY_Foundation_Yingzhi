import { request } from './http'
import type { PageResponse } from '../types/common'

export interface PaymentItem {
  paymentNo: string
  orderNo: string
  channel: string
  amount: string
  status: string
  createdAt: string
  currency?: string
  paidAt?: string
}

// 获取支付交易记录列表（分页）
export function listPayments(page = 1, pageSize = 20) {
  return request<PageResponse<PaymentItem>>(`/admin/payments?page=${page}&pageSize=${pageSize}`)
}

// 获取支付详情
export function getPayment(paymentNo: string) {
  return request<PaymentItem>(`/admin/payments/${paymentNo}`)
}
