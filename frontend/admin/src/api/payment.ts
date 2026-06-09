import { request } from './http'
import type { PageResponse } from '../types/common'
import type { PaymentItem } from '@shared/types/payment'
export type { PaymentItem }

export function listPayments(page = 1, pageSize = 20) {
  return request<PageResponse<PaymentItem>>(`/admin/payments?page=${page}&pageSize=${pageSize}`)
}

export function getPayment(paymentNo: string) {
  return request<PaymentItem>(`/admin/payments/${paymentNo}`)
}
