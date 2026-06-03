export interface PlanGroup {
  id: number
  code: string
  name: string
  description: string
  status: string
  sortOrder: number
  plans: Plan[]
}

export interface Plan {
  id: number
  code: string
  name: string
  description: string
  badge: string
  status: string
  sortOrder: number
  prices: Price[]
  features: Feature[]
}

export interface Price {
  id: number
  currency: string
  billingCycle: string
  amount: string
  originalAmount?: string
  status: string
}

export interface Feature {
  id: number
  featureName: string
  featureValue: string
  included: boolean
  sortOrder: number
}
