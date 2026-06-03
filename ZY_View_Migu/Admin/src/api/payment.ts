import { request } from './http'

export interface PaymentItem {
  paymentNo: string
  orderNo: string
  channel: string
  amount: string
  status: string
  createdAt: string
}

// 获取支付交易记录列表
export function listPayments() {
  console.log('[API] listPayments')
  return request<PaymentItem[]>('/admin/payments')
}
