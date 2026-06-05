import { request } from './http'

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

// 获取支付交易记录列表
export function listPayments() {
  return request<PaymentItem[]>('/admin/payments')
}

// 获取支付详情
export function getPayment(paymentNo: string) {
  return request<PaymentItem>(`/admin/payments/${paymentNo}`)
}
