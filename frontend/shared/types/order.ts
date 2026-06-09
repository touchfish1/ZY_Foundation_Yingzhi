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
