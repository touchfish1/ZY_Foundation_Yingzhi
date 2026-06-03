import { request } from './http'

export interface OrderItem {
  orderNo: string
  amount: string
  currency: string
  status: string
  createdAt: string
}

// 获取订单列表
export function listOrders() {
  console.log('[API] listOrders')
  return request<OrderItem[]>('/admin/orders')
}
