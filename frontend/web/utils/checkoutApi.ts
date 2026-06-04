interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface CheckoutOrderPayload {
  planCode: string
  priceId?: number | string
  billingCycle?: string
  quantity: number
}

export interface CheckoutOrder {
  id?: number | string
  orderId?: number | string
  orderNo?: string
}

export interface CheckoutPaymentPayload {
  orderId: number | string
}

export interface CheckoutPayment {
  checkoutUrl?: string
  payUrl?: string
  paymentUrl?: string
}

async function apiRequest<T>(apiBase: string, path: string, payload: any) {
  const response = await $fetch<ApiResponse<T>>(`${apiBase}${path}`, {
    method: 'POST',
    body: payload
  })

  if (response.code !== 0) {
    throw new Error(response.message || '接口请求失败')
  }

  return response.data
}

export function createCheckoutOrder(apiBase: string, payload: CheckoutOrderPayload) {
  return apiRequest<CheckoutOrder>(apiBase, '/api/orders', payload)
}

export function createCheckoutPayment(apiBase: string, payload: CheckoutPaymentPayload) {
  return apiRequest<CheckoutPayment>(apiBase, '/api/payments/checkout', payload)
}
