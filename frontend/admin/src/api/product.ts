import { request } from './http'
import type { PlanGroup, Plan, Price, Feature } from '@shared/types/product'
export type { PlanGroup, Plan, Price, Feature }

export function listPlanGroups() {
  return request<PlanGroup[]>('/admin/product/plan-groups')
}

export function createPlanGroup(payload: { code: string; name: string; description: string; sortOrder: number }) {
  return request<PlanGroup>('/admin/product/plan-groups', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function listPlans() {
  return request<Plan[]>('/admin/product/plans')
}

export function listPrices() {
  return request<Price[]>('/admin/product/prices')
}

export function createPlan(payload: { groupId: number; code: string; name: string; description: string; badge: string; sortOrder: number }) {
  return request<Plan>('/admin/product/plans', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function createPrice(payload: { planId: number; currency: string; billingCycle: string; amount: number; originalAmount?: number | null }) {
  return request<Price>('/admin/product/prices', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function createFeature(payload: { planId: number; featureName: string; featureValue: string; included: boolean; sortOrder: number }) {
  return request<Feature>('/admin/product/features', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
