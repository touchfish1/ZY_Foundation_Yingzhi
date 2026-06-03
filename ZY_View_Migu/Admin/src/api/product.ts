import { request } from './http'

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
  groupId: number
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

// 获取套餐组列表（含每个组下的套餐、价格、权益）
export function listPlanGroups() {
  console.log('[API] listPlanGroups')
  return request<PlanGroup[]>('/admin/product/plan-groups')
}

// 创建新的套餐组
export function createPlanGroup(payload: { code: string; name: string; description: string; sortOrder: number }) {
  console.log('[API] createPlanGroup', payload)
  return request<PlanGroup>('/admin/product/plan-groups', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

// 获取所有套餐列表
export function listPlans() {
  console.log('[API] listPlans')
  return request<Plan[]>('/admin/product/plans')
}

// 获取所有价格记录列表
export function listPrices() {
  console.log('[API] listPrices')
  return request<Price[]>('/admin/product/prices')
}

// 在指定套餐组下创建新套餐
export function createPlan(payload: { groupId: number; code: string; name: string; description: string; badge: string; sortOrder: number }) {
  console.log('[API] createPlan', payload)
  return request<Plan>('/admin/product/plans', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

// 为指定套餐添加定价（币种、周期、金额）
export function createPrice(payload: { planId: number; currency: string; billingCycle: string; amount: number; originalAmount?: number | null }) {
  console.log('[API] createPrice', payload)
  return request<Price>('/admin/product/prices', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

// 为指定套餐添加权益项
export function createFeature(payload: { planId: number; featureName: string; featureValue: string; included: boolean; sortOrder: number }) {
  console.log('[API] createFeature', payload)
  return request<Feature>('/admin/product/features', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
