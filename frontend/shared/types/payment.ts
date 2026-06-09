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

export interface CheckoutResponse {
  paymentNo: string
  status: string
  payUrl?: string
  checkoutUrl?: string
  channel?: string
}
